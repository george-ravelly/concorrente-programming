package atomic;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class AtomicFileLoader {
    private static final Lock lock = new ReentrantLock();

    public static Instances fileLoader (String filePath) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        LongAdder count = new LongAdder();

        attributes.add(new Attribute("feature1")); // Atributo texto
        attributes.add(new Attribute("feature2")); // Atributo texto
        attributes.add(new Attribute("feature3")); // Atributo texto

        // Atributo categórico com classes 1, 2
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("0");
        classValues.add("1");
        attributes.add(new Attribute("label", classValues));

        // Cria a estrutura do dataset
        AtomicReference<Instances> dataset = new AtomicReference<>(new Instances("large_dataset", attributes, 0));

        try (
                var executorService = Executors.newVirtualThreadPerTaskExecutor();
                var reader = Files.newBufferedReader(Path.of(filePath))
        ) {
            dataset.get().setClassIndex(dataset.get().numAttributes() - 1);
            Stream<String> lines = reader.lines();

            lines.forEach(line -> {
                executorService.submit(() -> {
                    String[] values = line.split(",");

                    if (values.length == 4) {
                        DenseInstance instance = new DenseInstance(dataset.get().numAttributes());

                        instance.setValue(attributes.get(0), Double.parseDouble(values[0])); //
                        instance.setValue(attributes.get(1), Double.parseDouble(values[1])); //
                        instance.setValue(attributes.get(2), Double.parseDouble(values[2])); //
                        instance.setValue(attributes.get(3), Double.parseDouble(values[3])); //

                        // Início da seção crítica
//                        lock.lock();
                        try {
                            instance.setDataset(dataset.get());
                            dataset.updateAndGet(d -> {
                                d.add(instance);
                                return d;
                            });
                            count.increment();
                        } finally {
                            // Liberação do lock para permitir que outra thread acesse a seção crítica
//                            lock.unlock();
                        }
                    }

                });
            });
            lines.close();
            executorService.shutdown();
            System.out.println(count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return dataset.get();
    }
}
