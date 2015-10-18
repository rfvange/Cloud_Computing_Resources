[root@sandbox cloudapp-mp5]# cat src/ShortestPathsComputation.java
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * Compute shortest paths from a given source.
 */
public class ShortestPathsComputation extends BasicComputation<
    IntWritable, IntWritable, NullWritable, IntWritable> {
  /** The shortest paths id */
  public static final LongConfOption SOURCE_ID =
      new LongConfOption("SimpleShortestPathsVertex.sourceId", 1,
          "The shortest paths id");

  /**
   * Is this vertex the source id?
   *
   * @param vertex Vertex
   * @return True if the source id
   */
  private boolean isSource(Vertex<IntWritable, ?, ?> vertex) {
    return vertex.getId().get() == SOURCE_ID.get(getConf());
  }

  @Override
  public void compute(
      Vertex<IntWritable, IntWritable, NullWritable> vertex,
      Iterable<IntWritable> messages) throws IOException {
    if (getSuperstep() == 0) {
      vertex.setValue(new IntWritable(Integer.MAX_VALUE));
    }
    Integer minDist = isSource(vertex) ? 0 : Integer.MAX_VALUE;
    for (IntWritable message : messages) {
      minDist = Math.min(minDist, message.get());
    }

    if (minDist < vertex.getValue().get()) {
      vertex.setValue(new IntWritable(minDist));
      for (Edge<IntWritable, NullWritable> edge : vertex.getEdges()) {
        Integer distance = minDist + 1; //edge.getValue().get();
        sendMessage(edge.getTargetVertexId(), new IntWritable(distance));
      }
    }
    vertex.voteToHalt();

  }
}
[root@sandbox cloudapp-mp5]#



/* See:
    https://www.codatlas.com/github.com/apache/giraph/trunk/giraph-examples/src/main/java/org/apache/giraph/examples/SimpleShortestPathsComputation.java

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
*/

import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.conf.LongConfOption;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * Compute shortest paths from a given source.
 */
public class ShortestPathsComputation extends BasicComputation<
    IntWritable, IntWritable, NullWritable, IntWritable> {
  /** The shortest paths id */
  public static final LongConfOption SOURCE_ID =
      new LongConfOption("SimpleShortestPathsVertex.sourceId", 1,
          "The shortest paths id");

  /**
   * Is this vertex the source id?
   *
   * @param vertex Vertex
   * @return True if the source id
   */
  private boolean isSource(Vertex<IntWritable, IntWritable, NullWritable> vertex) {
    return vertex.getId().get() == SOURCE_ID.get(getConf());
  }

  @Override
  public void compute(
      Vertex<IntWritable, IntWritable, NullWritable> vertex,
      Iterable<IntWritable> messages) throws IOException {
    if (getSuperstep() == 0) {
      vertex.setValue(new IntWritable(Double.MAX_VALUE));
    }
    double minDist = isSource(vertex) ? 0d : Double.MAX_VALUE;
    for (IntWritable message : messages) {
      minDist = Math.min(minDist, message.get());
    }

    if (minDist < vertex.getValue().get()) {
      vertex.setValue(new IntWritable(minDist));
      for (Edge<IntWritable, IntWritable> edge : vertex.getEdges()) {
        double distance = minDist + edge.getValue().get();
        sendMessage(edge.getTargetVertexId(), new IntWritable(distance));
      }
    }
    vertex.voteToHalt();
  }
}