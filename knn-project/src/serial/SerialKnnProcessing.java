package serial;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;

public class SerialKnnProcessing {
    public static void knnProcessing (Instances data, int numInstances, double trainLength, double testLength) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        int trainSize = (int) Math.round(numInstances * trainLength);
        int testSize = (int) Math.round(trainSize * testLength);
        System.out.println("Train: "+  trainSize + ", Test: " + testSize);
        int chunkSize = testSize / numThreads;
        data.randomize(new Random(42));  // Shuffle dos dados

        Instances trainData = new Instances(data, 0, trainSize);
        Instances testData = new Instances(data, trainSize, testSize);

        data = null;
        try {
        // Criar e configurar o modelo KNN
            IBk knn = new IBk();
            knn.setKNN(5);  // Definir o número de vizinhos (K)
            knn.buildClassifier(trainData);
            Evaluation eval = new Evaluation(trainData);
            double precision = 0;
            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? testSize : (i + 1) * chunkSize;

                Instances chunkTestData = new Instances(testData, start, end - start);
                eval.evaluateModel(knn, chunkTestData);
                precision += eval.pctCorrect();

            }
            double averagePrecision = precision / numThreads;
            System.out.println("Acurácia média do modelo KNN: " + averagePrecision + "%");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
