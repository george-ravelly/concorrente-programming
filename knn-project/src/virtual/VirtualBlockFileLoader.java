package virtual;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class VirtualBlockFileLoader {
    private static final Lock lock = new ReentrantLock();

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

        // Cria a estrutura do dataset
        Instances dataset = new Instances("large_dataset", attributes, 0);

        try (
                var executorService = Executors.newVirtualThreadPerTaskExecutor();
                var reader = Files.newBufferedReader(Path.of(filePath))
        ) {
            long linesPerThread = 300000;
            LongAdder lineCount = new LongAdder();
            dataset.setClassIndex(dataset.numAttributes() - 1);
            Stream<String> lines = reader.lines();

            List<String> linesBuffer = new ArrayList<>();

            lines.forEach(l -> {
                linesBuffer.add(l);
                lineCount.increment();
                if (lineCount.longValue() == linesPerThread)  {
                    List<String> tempLinesBuffer = new ArrayList<>(linesBuffer);
                    executorService.submit(() -> processBlock(tempLinesBuffer, dataset, attributes));
                    linesBuffer.clear();
                    lineCount.reset();
                }
            });
            // Adiciona qualquer bloco restante para processamento
            if (!linesBuffer.isEmpty()) {
                List<String> linesToProcess = new ArrayList<>(linesBuffer);
                executorService.submit(() -> processBlock(linesToProcess, dataset, attributes));
            }
            lines.close();
            executorService.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataset;
    }

    public static void processBlock(List<String> linesBuffer, Instances dataset, ArrayList<Attribute> attributes) {
        linesBuffer.forEach(line -> {
            String[] values = line.split(",");

            if (values.length == 4) {
                DenseInstance instance = new DenseInstance(dataset.numAttributes());

                instance.setValue(attributes.get(0), Double.parseDouble(values[0])); //
                instance.setValue(attributes.get(1), Double.parseDouble(values[1])); //
                instance.setValue(attributes.get(2), Double.parseDouble(values[2])); //
                instance.setValue(attributes.get(3), Double.parseDouble(values[3])); //

                // Início da seção crítica
                lock.lock();
                try {
                    instance.setDataset(dataset);
                    dataset.add(instance);
                } finally {
                    // Liberação do lock para permitir que outra thread acesse a seção crítica
                    lock.unlock();
                }
            }
        });
    }
}
