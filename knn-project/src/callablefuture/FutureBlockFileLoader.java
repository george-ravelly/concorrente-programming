package callablefuture;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class FutureBlockFileLoader {
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

            List<Future<List<DenseInstance>>> futureList = new ArrayList<>();

            for (String l : lines.toList()) {
                linesBuffer.add(l);
                lineCount.increment();
                if (lineCount.longValue() == linesPerThread)  {
                    List<String> tempLinesBuffer = new ArrayList<>(linesBuffer);
                    futureList.add(executorService.submit(() -> processBlock(tempLinesBuffer, attributes)));
                    linesBuffer.clear();
                    lineCount.reset();
                }
            }
            lines.close();

            // Adiciona qualquer bloco restante para processamento
            if (!linesBuffer.isEmpty()) {
                List<String> linesToProcess = new ArrayList<>(linesBuffer);
                futureList.add(executorService.submit(() -> processBlock(linesToProcess, attributes)));
            }

            for (Future<List<DenseInstance>> denseInstances : futureList) {
                try {
                    List<DenseInstance> instances = denseInstances.get();
                    for (DenseInstance inst : instances) {
                        if (inst != null) {
                            inst.setDataset(dataset);
                            dataset.add(inst);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            executorService.shutdown();

            try {
                if (executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.out.println("Concluído!");
                } else {
                    System.err.println("Tempo limite excedido para as threads serem concluídas.");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataset;
    }

    public static List<DenseInstance> processBlock(List<String> linesBuffer, ArrayList<Attribute> attributes) {
        var lista = linesBuffer.stream().map(line -> {
            String[] values = line.split(",");

            if (values.length == 4) {
                DenseInstance instance = new DenseInstance(attributes.size());
                instance.setValue(attributes.get(0), Double.parseDouble(values[0])); //
                instance.setValue(attributes.get(1), Double.parseDouble(values[1])); //
                instance.setValue(attributes.get(2), Double.parseDouble(values[2])); //
                instance.setValue(attributes.get(3), Double.parseDouble(values[3])); //
                return instance;
            }
            return null;
        }).toList();
        return lista;
    }
}
