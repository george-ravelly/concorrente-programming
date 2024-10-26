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

        attributes.add(new Attribute("feature1")); // Atributo texto
        attributes.add(new Attribute("feature2")); // Atributo texto
        attributes.add(new Attribute("feature3")); // Atributo texto

        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("0");
        classValues.add("1");
        attributes.add(new Attribute("label", classValues));


        // Cria a estrutura do dataset
        Instances dataset = new Instances("large_dataset", attributes, 0);

        Runnable runner = () -> {
            // Define os atributos (colunas)
            // Define a última coluna como a classe
            dataset.setClassIndex(dataset.numAttributes() - 1);

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
                        String[] values = line.split(",");

                        if (values.length == 4) {
                            // Cria uma nova instância e preenche os valores
                            DenseInstance instance = new DenseInstance(dataset.numAttributes());

                            instance.setValue(attributes.get(0), Double.parseDouble(values[0])); //
                            instance.setValue(attributes.get(1), Double.parseDouble(values[1])); //
                            instance.setValue(attributes.get(2), Double.parseDouble(values[2])); //
                            instance.setValue(attributes.get(3), Double.parseDouble(values[3])); //

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

