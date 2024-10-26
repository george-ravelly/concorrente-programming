import utils.PreProcessing;
import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        // Carregar e pré-processar os dados
        long tempoInicial = System.currentTimeMillis();
        System.out.println("Iniciando carregamento e pré-processamento!");
        Instances data = TextPreprocessing
                .loadData("/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/large_dataset.arff");
        System.out.println("Arquivo carregado: " + ((System.currentTimeMillis() - tempoInicial)) + "ms");

        Runnable runnable = () -> {
            try {
                // Aplicando o filtro StringToWordVector para transformar texto em vetores
                // Configurar o PCA
                Instances pcaInstances = PreProcessing.pca(data);

                // Dividir os dados em treino e teste (80% treino, 20% teste)
                int trainSize = (int) Math.round(pcaInstances.numInstances() * 0.8);
                int testSize = (int) Math.round(trainSize * 0.2);
                pcaInstances.randomize(new Random(42));  // Shuffle dos dados
    
                Instances trainData = new Instances(pcaInstances, 0, trainSize);
                Instances testData = new Instances(pcaInstances, trainSize, testSize);
    
                // Criar e configurar o modelo KNN
                IBk knn = new IBk();
                knn.setKNN(5);  // Definir o número de vizinhos (K)

                knn.buildClassifier(trainData);
                Evaluation eval = new Evaluation(trainData);
                eval.evaluateModel(knn, testData);

                System.out.println("Acurácia do modelo KNN: " + eval.pctCorrect() + "%");
                System.out.println(eval.toSummaryString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        System.out.println("Iniciando processamento: " + ((System.currentTimeMillis() - tempoInicial)) + "ms");
        var builder = Thread.ofPlatform().name("processing", 1).start(runnable);

        while (true) {
            if (!builder.isAlive()) {
                break;
            }
        }

        System.out.println("finalizado! "  + ((System.currentTimeMillis() - tempoInicial)) + "ms");
    }
}