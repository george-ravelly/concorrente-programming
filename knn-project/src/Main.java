import weka.attributeSelection.PrincipalComponents;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Random;

public class Main {
    public static void main(String[] args) throws Exception {
        // Carregar e pré-processar os dados
        long tempoInicial = System.currentTimeMillis();
        System.out.println("Iniciando carregamento e pré-processamento!");
        Instances data = TextPreprocessing
                .loadData("/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/arquivoTrain.arff");
        System.out.println("Arquivo carregado: " + ((System.currentTimeMillis() - tempoInicial)) + "ms");

        Runnable runnable = () -> {
            Instances filteredData = null;
            try {
                // Aplicando o filtro StringToWordVector para transformar texto em vetores
                StringToWordVector filter = new StringToWordVector();
                filter.setInputFormat(data);
                filter.setTFTransform(true);  // Para aplicar TF-IDF
                filter.setIDFTransform(true);
                filter.setLowerCaseTokens(true);  // Para considerar apenas letras minúsculas

                // Filtrando os dados
                filteredData = Filter.useFilter(data, filter);
                // Configurar o PCA
//                PrincipalComponents pca = new PrincipalComponents();
//                pca.setCenterData(true); // Centralizar os dados antes de realizar o PCA
//                pca.setVarianceCovered(0.95); // Mantém componentes que cobrem até 95% da variância
//                pca.buildEvaluator(filteredData);
//
//                // Aplicar o filtro de PCA para transformar os dados originais
//                filteredData = pca.transformedData(filteredData);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Dividir os dados em treino e teste (80% treino, 20% teste)
            int trainSize = (int) Math.round(filteredData.numInstances() * 0.8);
            int testSize = (int) Math.round(trainSize * 0.2);
            filteredData.randomize(new Random(42));  // Shuffle dos dados

            Instances trainData = new Instances(filteredData, 0, trainSize);
            Instances testData = new Instances(filteredData, trainSize, testSize);

            // Criar e configurar o modelo KNN
            IBk knn = new IBk();
            knn.setKNN(5);  // Definir o número de vizinhos (K)

            // Treinar o modelo
            try {
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