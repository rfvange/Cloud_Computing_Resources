https://gist.github.com/erikerlandson/794c16e94f7fe1c42d98

// starting Spark REPL at top of source tree:
// $ cd ~/git/spark
// $ ./bin/spark-shell
//
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.util.MLUtils
// Load and parse the data file.
val data = MLUtils.loadLibSVMFile(sc, "data/mllib/sample_libsvm_data.txt")
// Split the data into training and test sets (30% held out for testing)
val splits = data.randomSplit(Array(0.7, 0.3))
val (trainingData, testData) = (splits(0), splits(1))
// Train a RandomForest model.
// Empty categoricalFeaturesInfo indicates all features are continuous.
val numClasses = 2
val categoricalFeaturesInfo = Map[Int, Int]()
val numTrees = 3 // Use more in practice.
val featureSubsetStrategy = "auto" // Let the algorithm choose.
val impurity = "gini"
val maxDepth = 4
val maxBins = 32
val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
// Evaluate model on test instances and compute test error
val labelAndPreds = testData.map { point =>
    val prediction = model.predict(point.features)
    (point.label, prediction)
}
val testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / testData.count()
println("Test Error = " + testErr)
println("Learned classification forest model:\n" + model.toDebugString)

/*
Other good articles.
Search: "spark mllib randomforest".
https://www.mapr.com/blog/comparing-kill-mockingbird-its-sequel-with-apache-spark#.VhWaCnmFO00
http://www.duchess-france.org/analyze-accelerometer-data-with-apache-spark-and-mllib/
http://www.project2ist.com/

// * @param <I> Vertex id * @param <V> Vertex data * @param <E> Edge data
public interface Vertex<I extends WritableComparable, V extends Writable, E extends Writable> extends
    ImmutableClassesGiraphConfigurable<I, V, E> {

*/

// http://www.project2ist.com/2015/04/random-forest-using-apache-spark-mllib.html

from pyspark.mllib.tree import RandomForest
from pyspark.mllib.util import MLUtils
 
data = MLUtils.loadLibSVMFile(sc, 'file:///home/cosmin/meetup/data/mushrooms')
(trainingData, testData) = data.randomSplit([0.7, 0.3])
 
model = RandomForest.trainClassifier(trainingData, numClasses=3, categoricalFeaturesInfo={},
                                     numTrees=3, featureSubsetStrategy="auto",
                                     impurity='gini', maxDepth=4, maxBins=32)
 
# Evaluate model on test instances and compute test error
predictions = model.predict(testData.map(lambda x: x.features))
labelsAndPredictions = testData.map(lambda lp: lp.label).zip(predictions)
testErr = labelsAndPredictions.filter(lambda (v, p): v != p).count() / float(testData.count())
print('Test Error = ' + str(testErr))
print('Learned classification forest model:')
print(model.toDebugString())

++++++

http://www.project2ist.com/2015/04/running-svm-on-spark.html

Running an SVM on spark 



Thing is, you can not train in a distributed manner an SVM (with a specific C and gamma) on apache spark, as far as I'm aware of. At some point the best combination of C and gamma need to be figured out, and the last part can be executed in a distributed manner.
 The example bellow generates random data and tries to train an SVM on the data.

 First off the old way of doing things:

import numpy as np
from sklearn import svm
 
train_count = 1000
test_count = train_count/4
 
# generate training  set
train_x = np.random.rand(train_count,5)
train_weights = np.random.randint(0,100,train_count)
train_y = np.random.randint(0,2,train_count)
 
# generate test set
test_x = np.random.rand(test_count,5)
test_y = np.random.randint(0,2,test_count)
 
clf_weights = svm.SVC(C=2.0, gamma=0.1)
print clf_weights.fit(train_x, train_y, sample_weight=train_weights)
 
for C in range(-5, 15):  # C values
    for gamma in range(-15, 3):  # gamma values
        clf_weights = svm.SVC(C=pow(2, C), gamma=pow(2, gamma))
        clf_weights.fit(train_x, train_y, sample_weight=train_weights)
 
        test_result = clf_weights.predict(test_x)
        count = 0
        for i in range(test_result.size):
            if test_y[i] == test_result[i]:
                count += 1
 
        print "C=" + str(pow(2, C)) + " gamma=" + str(pow(2, gamma)) + " validCound=" + str(count)
 
print train_x
print train_weights
print train_y
 
The spark way of doing it:

import os
import numpy as np
from sklearn import svm
from pyspark import SparkContext, SparkConf
 
def trainAndTestSVM(C, gamma):
    train_count = 200
    test_count = train_count/4
 
    # generate training  set normally this data should be read from HDFS
    train_x = np.random.rand(train_count,5)
    train_weights = np.random.randint(0,100,train_count)
    train_y = np.random.randint(0,2,train_count)
 
    # generate test set
    test_x = np.random.rand(test_count,5)
    test_y = np.random.randint(0,2,test_count)
 
    clf_weights = svm.SVC(C=pow(2, C), gamma=pow(2, gamma))
    clf_weights.fit(train_x, train_y, sample_weight=train_weights)
 
    test_result = clf_weights.predict(test_x)
    count = 0
    for i in range(test_result.size):
        if test_y[i] == test_result[i]:
            count += 1
 
    return [C, gamma, count]
 
from pyspark import SparkContext
conf = SparkConf().setMaster('spark://CentOS-65-64-minimal:7077').setAppName('RunTest').set('spark.cores.max', 8)
sc = SparkContext(conf=conf, pyFiles=['svm_onSpark.py'])
 
svmParams = []
for C in range(-5, 10):  # C values
    for gamma in range(-15, 3):  # gamma values
        svmParams.append([pow(2, C), pow(2, gamma)])
 
rddSvmParams = sc.parallelize(svmParams, len(svmParams))
result = rddSvmParams.map(lambda params: trainAndTestSVM(params[0], params[1]))
cResult = result.collect()
 
 
print cResult

++++++
 
http://www.project2ist.com/2015/04/sentiment-analysis-using-stanford-core.html

Sentiment analysis using Stanford core NLP 




 Can view how it on o live demo here : http://nlp.stanford.edu:8080/sentiment/rntnDemo.html
 It took a while for me to setup the project because it needed some resources which I knew nothing about.
 I went on and added some reviews from amazon.com to see if I the text given by the reviewer matches the number of stars and it kinda does. Overall I found that it works quite well.


    String line = "Great item! HDMI and decent wifi required as with all streaming devices.\n" +
            "The flow on the homepage is very good and responsive. Watching a series is a doddle, flow is great, no action required.\n" +
            "The remote and controller app both work a treat.\n" +
            "I really like this device.\n" +
            "I'd like to see an Amazon-written mirroring app available for non-Amazon products but no-one likes talking to each other in this field!";
 
    Long textLength = 0L;
    int sumOfValues = 0;
 
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
    int mainSentiment = 0;
    if (line != null && line.length() > 0) {
        int longest = 0;
        Annotation annotation = pipeline.process(line);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            String partText = sentence.toString();
            if (partText.length() > longest) {
                textLength += partText.length();
                sumOfValues = sumOfValues + sentiment * partText.length();
 
                System.out.println(sentiment + " " + partText);
            }
        }
    }
 
    System.out.println("Overall: " + (double)sumOfValues/textLength);
}
 
Adding annotator tokenize
Adding annotator ssplit
Adding annotator parse
Loading parser from serialized file edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz ... done [1.4 sec].
Adding annotator sentiment
3 Great item!
3 HDMI and decent wifi required as with all streaming devices.
4 The flow on the homepage is very good and responsive.
3 Watching a series is a doddle, flow is great, no action required.
3 The remote and controller app both work a treat.
1 I really like this device.
1 I'd like to see an Amazon-written mirroring app available for non-Amazon products but no-one likes talking to each other in this field!
Overall: 2.3241206030150754




