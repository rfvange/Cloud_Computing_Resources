[root@sandbox cloudapp-mp5]# cat src/RandomForestMP.java
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
[root@sandbox cloudapp-mp5]#



++++++

// Search: "apache spark mllib randomforest"

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
        JavaRDD<LabeledPoint> test = sc.textFile(test_data_path).map(new DataToPoint());

//        JavaRDD<Vector> points = lines.map(new ParsePoint());
//        JavaRDD<String> titles = lines.map(new ParseTitle());

//        model = RandomForestModel.train(points.rdd(), k, iterations, runs, RandomForestModel.RANDOM(), 0);

// http://stackoverflow.com/questions/28971989/pyspark-mllib-random-forest-feature-importances
		
        model = RandomForest.trainClassifier(trainingData.rdd(), numClasses, categoricalFeaturesInfo,
                                     numTrees, featureSubsetStrategy,
                                     impurity, maxDepth, maxBins);

        JavaRDD<LabeledPoint> results = test.map(new Function<Vector, LabeledPoint>() {
            public LabeledPoint call(Vector points) {
                return new LabeledPoint(model.predict(points), points);
            }
        });

//        results.foreach(new PrintCluster(model));
        results.saveAsTextFile(results_path);

        sc.stop();
    }

}

++++++

[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.484 s
[INFO] Finished at: 2015-10-09T22:12:12+00:00
[INFO] Final Memory: 42M/334M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project mp5: Compilation failure
[ERROR] /root/cloudapp-mp5/src/RandomForestMP.java:[71,45] method map in class org.apache.spark.api.java.AbstractJavaRDDLike<T,This> cannot be applied to given types;
[ERROR] required: org.apache.spark.api.java.function.Function<org.apache.spark.mllib.regression.LabeledPoint,R>
[ERROR] found: <anonymous org.apache.spark.api.java.function.Function<org.apache.spark.mllib.linalg.Vector,org.apache.spark.mllib.regression.LabeledPoint>>
[ERROR] reason: no instance(s) of type variable(s) R exist so that argument type <anonymous org.apache.spark.api.java.function.Function<org.apache.spark.mllib.linalg.Vector,org.apache.spark.mllib.regression.LabeledPoint>> conforms to formal parameter type org.apache.spark.api.java.function.Function<org.apache.spark.mllib.regression.LabeledPoint,R>
[ERROR] -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
[root@sandbox cloudapp-mp5]# vi src/RandomForestMP.java
[root@sandbox cloudapp-mp5]# cat src/RandomForestMP.java
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
[root@sandbox cloudapp-mp5]#

