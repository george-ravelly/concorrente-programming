import weka.core.Instances;
import weka.core.converters.ConverterUtils.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextPreprocessing {
    public static Instances loadData(String filePath) {
        final Instances[] data = new Instances[1];
        Runnable executor = () -> {
            try {
                DataSource source = new DataSource(filePath);
                data[0] = source.getDataSet();
            } catch (Exception e) {
                System.out.println("erro");
                System.out.println(e.getMessage());
            }
        };

        var builder = Thread.ofVirtual().name("loading-db", 1).start(executor);

        try {
            while (true) {
                if (!builder.isAlive()) break;
//                System.out.println("waiting...");
            }
            // Setando a última coluna como a classe (polarity)
            if (data[0] != null) {
                data[0].setClassIndex(0);
            }

            // Aplicando o filtro StringToWordVector para transformar texto em vetores
            StringToWordVector filter = new StringToWordVector();
            filter.setInputFormat(data[0]);
            filter.setTFTransform(true);  // Para aplicar TF-IDF
            filter.setIDFTransform(true);
            filter.setLowerCaseTokens(true);  // Para considerar apenas letras minúsculas

            // Filtrando os dados
            return Filter.useFilter(data[0], filter);
        } catch (Exception e) {
            System.out.println("Erro filtrando dados: \n" + e.getMessage());
            return null;
        }
    }

}

