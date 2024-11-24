package main.ufrn.app.forkandjoin;

import main.ufrn.app.utils.PreprocessData;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ForkAndJoinKnn {

    public static class KnnTask extends RecursiveTask<Double> {
        private static final int CHUNK_SIZE_THRESHOLD = 500;  // Define o tamanho mínimo do chunk
        private final IBk knn;
        private final Evaluation eval;
        private final Instances testData;
        private final int start;
        private final int end;
        private final ReentrantLock lock = new ReentrantLock();

        public KnnTask(IBk knn, Evaluation eval, Instances testData, int start, int end) {
            this.knn = knn;
            this.eval = eval;
            this.testData = testData;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Double compute() {
            int size = end - start;
            if (size <= CHUNK_SIZE_THRESHOLD) {
                // Caso base: processar o chunk diretamente
                this.lock.lock();
                try {
                    eval.evaluateModel(knn, new Instances(testData, start, size));
                    return eval.pctCorrect();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    this.lock.unlock();
                }
            } else {
                // Dividir o chunk em duas partes e processar recursivamente
                int mid = start + size / 2;
                final KnnTask leftTask = new KnnTask(knn, eval, testData, start, mid);
                final KnnTask rightTask = new KnnTask(knn, eval, testData, mid, end);

                // Executar as duas subtarefas em paralelo
                leftTask.fork();
                final double rightResult = rightTask.compute();
                final double leftResult = leftTask.join();

                // Combinar os resultados das duas subtarefas
                return (leftResult + rightResult) / 2;
            }
        }
    }

    public static void knnProcessing(
            Instances data,
            int numInstances,
            double trainLength,
            double testLength,
            int k,
            int time
    ) {
        final int numThreads = Runtime.getRuntime().availableProcessors();
        try (ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads)) {
            // Dividir os dados em treino e teste (80% treino, 20% teste)
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

            // Criar e configurar o modelo KNN
            final IBk knn = new IBk();
            knn.setKNN(k);
            final Evaluation eval;
            try {
                knn.buildClassifier(trainData);
                eval = new Evaluation(trainData);
            } catch (Exception e) {
                throw new RuntimeException("Falha ao treinar o modelo KNN.", e);
            }

            trainData = null;  // Liberar memória do conjunto de treino

            // Executar a tarefa principal recursiva
            KnnTask mainTask = new KnnTask(knn, eval, testData, 0, testSize);

            try {
                double averagePrecision = forkJoinPool.invoke(mainTask);
                forkJoinPool.shutdown();
                if (forkJoinPool.awaitTermination(time, TimeUnit.SECONDS)) {
                    System.out.println("Acurácia média do modelo KNN: " + averagePrecision + "%");
                } else {
                    System.err.println("Tempo limite excedido para as tarefas serem concluídas.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}