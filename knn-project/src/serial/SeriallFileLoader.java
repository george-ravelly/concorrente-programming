package serial;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class SeriallFileLoader {
    public static Instances fileLoader (String filePath) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        attributes.add(new Attribute("feature1")); // Atributo texto
        attributes.add(new Attribute("feature2")); // Atributo texto
        attributes.add(new Attribute("feature3")); // Atributo texto

        // Atributo categórico com classes 1, 2
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("0");
        classValues.add("1");
        attributes.add(new Attribute("label", classValues));
        Instances dataset = new Instances("large_dataset", attributes, 0);

        try (
                var reader = Files.newBufferedReader(Path.of(filePath))
        ) {
            dataset.setClassIndex(dataset.numAttributes() - 1);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                String[] values = line.split(",");

                if (values.length == 4) {
                    final DenseInstance instance = new DenseInstance(attributes.size());
                    instance.setValue(attributes.get(0), Double.parseDouble(values[0])); //
                    instance.setValue(attributes.get(1), Double.parseDouble(values[1])); //
                    instance.setValue(attributes.get(2), Double.parseDouble(values[2])); //
                    instance.setValue(attributes.get(3), Double.parseDouble(values[3])); //
                    instance.setDataset(dataset);
                    dataset.add(instance);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataset;
    }
}
