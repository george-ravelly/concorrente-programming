package serial;

import utils.PreprocessData;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;

public class SerialKnnProcessing {
    public static void knnProcessing (Instances data, int numInstances, double trainLength, double testLength, int k, int time) throws Exception {
        int trainSize = (int) Math.round(numInstances * trainLength);

        data.randomize(new Random(42));

        Instances temp = new Instances(data, 0, trainSize);

        var pca = PreprocessData.applyPCA(temp, numInstances);

        trainSize = (int) Math.round(pca.numInstances() * trainLength);
        int testSize = (int) Math.round(trainSize * testLength);

        System.out.println("Train: " + trainSize + ", Test: " + testSize);

        data = null;

        final Instances testData = new Instances(pca, trainSize, testSize);

        Instances trainData = pca;

        pca = null;

        try {
        // Criar e configurar o modelo KNN
            IBk knn = new IBk();
            knn.setKNN(5);  // Definir o número de vizinhos (K)
            knn.buildClassifier(trainData);
            Evaluation eval = new Evaluation(trainData);

            eval.evaluateModel(knn, testData);

            double precision = eval.pctCorrect();
            System.out.println("Acurácia média do modelo KNN: " + precision + "%");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
