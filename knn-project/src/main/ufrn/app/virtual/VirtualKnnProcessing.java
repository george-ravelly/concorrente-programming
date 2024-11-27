package main.ufrn.app.virtual;

import main.ufrn.app.utils.PreprocessData;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualKnnProcessing {
    public static void knnProcessing (
            Instances data,
            int numInstances,
            double trainLength,
            double testLength,
            int k,
            int time
    ) {
        final int numThreads = Runtime.getRuntime().availableProcessors();
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
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
            final int chunkSize = testSize / numThreads;

            // Criar e configurar o modelo KNN
            final IBk knn = new IBk();
            knn.setKNN(k);  // Definir o número de vizinhos (K)
            knn.buildClassifier(trainData);
            DoubleAccumulator precision = new DoubleAccumulator(Double::sum, 0);
            final ReentrantLock lock = new ReentrantLock();
            final Evaluation eval = new Evaluation(trainData);
            trainData = null;
            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? testSize : (i + 1) * chunkSize;

                final Instances chunkTestData = new Instances(testData, start, end - start);

                // Criar tarefa para cada bloco
                executorService.execute(() -> {
                    lock.lock();
                    try {
                        eval.evaluateModel(knn, chunkTestData);
                        precision.accumulate(eval.pctCorrect());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        lock.unlock();
                    }
                });
            }

            // Finalizar a adição de tarefas e aguardar a conclusão
            executorService.shutdown();
            try {
                if (executorService.awaitTermination(time, TimeUnit.SECONDS)) {
                    // Calcular a média da precisão acumulada
                    double averagePrecision = precision.get() / numThreads;
                    System.out.println("Acurácia média do modelo KNN: " + averagePrecision + "%");
                } else {
                    System.err.println("Tempo limite excedido para as threads serem concluídas.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
