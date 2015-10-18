[root@sandbox cloudapp-mp5]# ls -l
total 84
-rw-r--r-- 1 root root  2406 2015-10-06 03:29 ConnectedComponentsComputation.java
-rw-r--r-- 1 root root     0 2015-10-07 05:12 [Help
drwxr-xr-x 3 root root  4096 2015-10-11 16:27 internal_use
-rw-r--r-- 1 root root  3505 2015-10-06 16:33 KMeansMP.java
-rw-r--r-- 1 root root 22264 2015-10-06 03:22 mp5.iml
-rw-r--r-- 1 root root  2279 2015-10-06 03:22 pom.xml
-rw-r--r-- 1 root root    15 2015-10-06 03:22 README.md
-rw-r--r-- 1 root root   355 2015-10-06 03:22 run_a.sh
-rw-r--r-- 1 root root   139 2015-10-06 03:22 run_b.sh
-rw-r--r-- 1 root root   391 2015-10-06 03:22 run_c.sh
-rw-r--r-- 1 root root   170 2015-10-06 03:22 run_d.sh
-rwxr-xr-x 1 root root   361 2015-10-06 03:22 settings.sh
-rw-r--r-- 1 root root  1651 2015-10-08 05:54 ShortestPathsComputation.java
drwxr-xr-x 2 root root  4096 2015-10-11 16:21 src
-rwxr-xr-x 1 root root    53 2015-10-06 03:22 start.sh
-rwxr-xr-x 1 root root   370 2015-10-06 03:22 submit.sh
drwxr-xr-x 7 root root  4096 2015-10-11 16:26 target
[root@sandbox cloudapp-mp5]# mv *.java src
[root@sandbox cloudapp-mp5]# ls -l src
total 16
-rw-r--r-- 1 root root 2406 2015-10-06 03:29 ConnectedComponentsComputation.java
-rw-r--r-- 1 root root 3505 2015-10-06 16:33 KMeansMP.java
-rw-r--r-- 1 root root 3587 2015-10-11 16:21 RandomForestMP.java
-rw-r--r-- 1 root root 1651 2015-10-08 05:54 ShortestPathsComputation.java
[root@sandbox cloudapp-mp5]# cat src/*.java
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;

import java.io.IOException;

/**
 * Implementation of the connected component algorithm that identifies
 * connected components and assigns each vertex its "component
 * identifier" (the smallest vertex id in the component).
 */
public class ConnectedComponentsComputation extends
    BasicComputation<IntWritable, IntWritable, NullWritable, IntWritable> {
  /**
   * Propagates the smallest vertex id to all neighbors. Will always choose to
   * halt and only reactivate if a smaller id has been sent to it.
   *
   * @param vertex Vertex
   * @param messages Iterator of messages from the previous superstep.
   * @throws IOException
   */
  @Override
  public void compute(
      Vertex<IntWritable, IntWritable, NullWritable> vertex,
      Iterable<IntWritable> messages) throws IOException {
      //TODO
    int currentComponent = vertex.getValue().get();

    // First superstep is special, because we can simply look at the neighbors
    if (getSuperstep() == 0) {
      for (Edge<IntWritable, NullWritable> edge : vertex.getEdges()) {
        int neighbor = edge.getTargetVertexId().get();
        if (neighbor < currentComponent) {
          currentComponent = neighbor;
        }
      }
      // Only need to send value if it is not the own id
      if (currentComponent != vertex.getValue().get()) {
        vertex.setValue(new IntWritable(currentComponent));
        for (Edge<IntWritable, NullWritable> edge : vertex.getEdges()) {
          IntWritable neighbor = edge.getTargetVertexId();
          if (neighbor.get() > currentComponent) {
            sendMessage(neighbor, vertex.getValue());
          }
        }
      }

      vertex.voteToHalt();
      return;
    }

    boolean changed = false;
    // did we get a smaller id ?
    for (IntWritable message : messages) {
      int candidateComponent = message.get();
      if (candidateComponent < currentComponent) {
        currentComponent = candidateComponent;
        changed = true;
      }
    }

    // propagate new component id to the neighbors
    if (changed) {
      vertex.setValue(new IntWritable(currentComponent));
      sendMessageToAllEdges(vertex, vertex.getValue());
    }
    vertex.voteToHalt();
  }
}
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import scala.Tuple2;


public final class KMeansMP {
        // TODO
    private static class ParsePoint implements Function<String, Vector> {
        private static final Pattern SPACE = Pattern.compile(",");

        public Vector call(String line) {
            String[] tok = SPACE.split(line);
            double[] point = new double[tok.length-1];
            for (int i = 1; i < tok.length; ++i) {
                point[i-1] = Double.parseDouble(tok[i]);
            }
            return Vectors.dense(point);
        }
    }

    private static class ParseTitle implements Function<String, String> {
        private static final Pattern SPACE = Pattern.compile(",");

        public String call(String line) {
            String[] tok = SPACE.split(line);
            return tok[0];
        }
    }

    private static class PrintCluster implements VoidFunction<Tuple2<Integer, Iterable<String>>> {
        private KMeansModel model;
        public PrintCluster(KMeansModel model) {
            this.model = model;
        }

        public void call(Tuple2<Integer, Iterable<String>> Cars) throws Exception {
            String ret = "[";
            for(String car: Cars._2()){
                ret += car + ", ";
            }
            System.out.println(ret + "]");
        }
    }


    private static class ClusterCars implements PairFunction<Tuple2<String, Vector>, Integer, String> {
        private KMeansModel model;
        public ClusterCars(KMeansModel model) {
            this.model = model;
        }

        public Tuple2<Integer, String> call(Tuple2<String, Vector> args) {
            String title = args._1();
            Vector point = args._2();
            int cluster = model.predict(point);
            return new Tuple2<Integer, String>(cluster, title);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "Usage: KMeansMP <input_file> <results>");
            System.exit(1);
        }
        String inputFile = args[0];
        String results_path = args[1];
//        JavaPairRDD<Integer, Iterable<String>> results;
        int k = 4;
        int iterations = 100;
        int runs = 1;
        long seed = 0;
        final KMeansModel model;

        SparkConf sparkConf = new SparkConf().setAppName("KMeans MP");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        //TODO
        JavaRDD<String> lines = sc.textFile(inputFile);

        JavaRDD<Vector> points = lines.map(new ParsePoint());
        JavaRDD<String> titles = lines.map(new ParseTitle());

        model = KMeans.train(points.rdd(), k, iterations, runs, KMeans.RANDOM(), 0);

        JavaPairRDD<Integer, Iterable<String>> results = titles.zip(points).mapToPair(new ClusterCars(model)).groupByKey();

        results.foreach(new PrintCluster(model));

        results.saveAsTextFile(results_path);

        sc.stop();
    }
}
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.mllib.tree.RandomForest;


import java.util.HashMap;
import java.util.regex.Pattern;

public final class RandomForestMP {

  private static class DataToPoint implements Function<String, LabeledPoint> {
    private static final Pattern SPACE = Pattern.compile(",");
    public LabeledPoint call(String line) throws Exception {
      String[] tok = SPACE.split(line);
      double label = Double.parseDouble(tok[tok.length-1]);
      double[] point = new double[tok.length-1];
      for (int i = 0; i < tok.length - 1; ++i) {
        point[i] = Double.parseDouble(tok[i]);
      }
      return new LabeledPoint(label, Vectors.dense(point));
    }
  }

    private static class ParsePoint implements Function<String, Vector> {
        private static final Pattern SPACE = Pattern.compile(",");

        public Vector call(String line) {
            String[] tok = SPACE.split(line);
            double[] point = new double[tok.length-1];
            for (int i = 0; i < tok.length-1; ++i) {
                point[i] = Double.parseDouble(tok[i]);
            }
            return Vectors.dense(point);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println(
                    "Usage: RandomForestMP <training_data> <test_data> <results>");
            System.exit(1);
        }
        String training_data_path = args[0];
        String test_data_path = args[1];
        String results_path = args[2];

        SparkConf sparkConf = new SparkConf().setAppName("RandomForestMP");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        final RandomForestModel model;

        Integer numClasses = 2;
        HashMap<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
        Integer numTrees = 3;
        String featureSubsetStrategy = "auto";
        String impurity = "gini";
        Integer maxDepth = 5;
        Integer maxBins = 32;
        Integer seed = 12345;

                // TODO

        JavaRDD<LabeledPoint> trainingData = sc.textFile(training_data_path).map(new DataToPoint());
        JavaRDD<Vector> test = sc.textFile(test_data_path).map(new ParsePoint());

//        JavaRDD<Vector> points = lines.map(new ParsePoint());
//        JavaRDD<String> titles = lines.map(new ParseTitle());

//        model = RandomForestModel.train(points.rdd(), k, iterations, runs, RandomForestModel.RANDOM(), 0);

// http://stackoverflow.com/questions/28971989/pyspark-mllib-random-forest-feature-importances

        model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
                                     numTrees, featureSubsetStrategy,
                                     impurity, maxDepth, maxBins, seed);

        JavaRDD<LabeledPoint> results = test.map(new Function<Vector, LabeledPoint>() {
            public LabeledPoint call(Vector points) {
                return new LabeledPoint(model.predict(points), points);
            }
        });

        results.saveAsTextFile(results_path);

        sc.stop();
    }

}
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
