package atomic;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.locks.ReentrantLock;

public class AtomicKnnProcessing {
    private static void knnProcessing (Instances data, int numInstances) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            // Dividir os dados em treino e teste (80% treino, 20% teste)
            int trainSize = (int) Math.round(numInstances * 0.01);
            int testSize = (int) Math.round(trainSize * 0.2);
            System.out.println("Train: "+  trainSize + ", Test: " + testSize);
            int chunkSize = testSize / numThreads;
            data.randomize(new Random(42));  // Shuffle dos dados

            Instances trainData = new Instances(data, 0, trainSize);
            Instances testData = new Instances(data, trainSize, testSize);

            data = null;

            // Criar e configurar o modelo KNN
            IBk knn = new IBk();
            knn.setKNN(5);  // Definir o número de vizinhos (K)
            knn.buildClassifier(trainData);
            DoubleAccumulator precision = new DoubleAccumulator(Double::sum, 0);
            ReentrantLock lock = new ReentrantLock();
            for (int i = 0; i < numThreads; i++) {
                int start = i * chunkSize;
                int end = (i == numThreads - 1) ? testSize : (i + 1) * chunkSize;

                Instances chunkTestData = new Instances(testData, start, end - start);

                // Criar tarefa para cada bloco
                executorService.execute(() -> {
                    try {
                        Evaluation eval = new Evaluation(trainData);
                        eval.evaluateModel(knn, chunkTestData);
                        precision.accumulate(eval.pctCorrect());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Finalizar a adição de tarefas e aguardar a conclusão
            executorService.shutdown();
            try {
                if (executorService.awaitTermination(60, TimeUnit.SECONDS)) {
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

    public static void main(String[] args) {
        String path = "/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/arquivoTest.arff";
        System.out.println("Carregando dados na memória! \n >> " + path);

        Instances data = AtomicFileLoader.fileLoader(path);

        System.out.println("Arquivo carregado!");
        System.out.println("Iniciando processamento dos dados! \n >> ");

        knnProcessing(data, data.numInstances());

    }
}
