package main.ufrn.app.benchmark;

import main.ufrn.app.callablefuture.FutureBlockFileLoader;
import main.ufrn.app.platform.PlatformBlockFileLoader;
import main.ufrn.app.serial.SeriallFileLoader;
import main.ufrn.app.virtual.VirtualBlockFileLoader;
import org.openjdk.jmh.annotations.*;
import weka.core.Instances;

import java.util.concurrent.TimeUnit;

public class FileLoaderBenchmark {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public Instances data;
        public String path = "/home/george/pessoal/Projetos/concurrent-programming/knn-project/src/main/resourses/large_dataset.arff";
        public int time = 120;

    }

    @Benchmark
    @Warmup(iterations = 3, time = 2)
    @Measurement(iterations = 3, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkSerialFileLoader(BenchmarkState state) {
        var temp = SeriallFileLoader.fileLoader(state.path);
        System.out.println("Arquivo carregado: " + temp.numInstances());
    }

    @Benchmark
    @Warmup(iterations = 3, time = 2)
    @Measurement(iterations = 3, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkVirtualFileLoader(BenchmarkState state) {
        var temp = VirtualBlockFileLoader.fileLoader(state.path);
        System.out.println("Arquivo carregado: " + temp.numInstances());
    }

    @Benchmark
    @Warmup(iterations = 3, time = 2)
    @Measurement(iterations = 3, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkPlatformFileLoader(BenchmarkState state) {
        var temp = PlatformBlockFileLoader.fileLoader(state.path);
        System.out.println("Arquivo carregado: " + temp.numInstances());
    }

    @Benchmark
    @Warmup(iterations = 3, time = 2)
    @Measurement(iterations = 3, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkCallableFutureFileLoader(BenchmarkState state) {
        var temp = FutureBlockFileLoader.fileLoader(state.path);
        System.out.println("Arquivo carregado: " + temp.numInstances());
    }

}
