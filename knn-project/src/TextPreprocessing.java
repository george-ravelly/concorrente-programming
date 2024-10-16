import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class TextPreprocessing {
    public static Instances loadData(String filePath) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        // Atributo categórico com classes 1, 2
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("1");
        classValues.add("2");
        attributes.add(new Attribute("polarity", classValues));

        attributes.add(new Attribute("title", (ArrayList<String>) null)); // Atributo texto
        attributes.add(new Attribute("text", (ArrayList<String>) null)); // Atributo texto


        // Cria a estrutura do dataset
        Instances dataset = new Instances("reviews", attributes, 0);

        Runnable runner = () -> {
            // Define os atributos (colunas)
            dataset.setClassIndex(0); // Define a última coluna como a classe

            // Lê o arquivo linha por linha
            BufferedReader reader = null;
            try {
                reader = Files.newBufferedReader(Path.of(filePath));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                    try {
                        // Divida a linha por vírgula ou outro delimitador
                        String[] values = line.replace("\"", "").split(",");

                        if (values.length == 3) {
                            // Cria uma nova instância e preenche os valores
                            DenseInstance instance = new DenseInstance(dataset.numAttributes());

                            instance.setValue(attributes.get(0), values[0]); // polarity
                            instance.setValue(attributes.get(1), values[1]); // title
                            instance.setValue(attributes.get(2), values[2]); // text

                            // Adiciona a instância ao dataset
                            instance.setDataset(dataset);
                            dataset.add(instance);
                        }

                    } catch (Exception e) {
                        System.out.println("aqui!");
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            // Agora, 'dataset' contém todas as instâncias lidas do arquivo
            System.out.println("Dataset carregado com " + dataset.numInstances() + " instâncias.");
        };

        var builder = Thread.ofVirtual().name("loading-db", 1).start(runner);

        while (true) {
            if (!builder.isAlive()) {
                System.out.println("Finalizado!");
                break;
            }
        }

        return dataset;
    }
}

