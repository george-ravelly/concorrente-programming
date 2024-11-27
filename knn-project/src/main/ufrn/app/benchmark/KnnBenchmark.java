package main.ufrn.app.benchmark;

import main.ufrn.app.atomic.AtomicKnnProcessing;
import main.ufrn.app.callablefuture.FutureKnnProcessing;
import main.ufrn.app.forkandjoin.ForkAndJoinKnn;
import main.ufrn.app.serial.SerialKnnProcessing;
import main.ufrn.app.serial.SeriallFileLoader;
import main.ufrn.app.virtual.VirtualKnnProcessing;
import org.openjdk.jmh.annotations.*;
import main.ufrn.app.platform.PlatformKnnProcessing;
import main.ufrn.app.virtual.VirtualBlockFileLoader;
import weka.core.Instances;

import java.util.concurrent.TimeUnit;

public class KnnBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public Instances data;
        public String path = "/home/george/pessoal/Projetos/concurrent-programming/knn-project/src/main/resourses/large_dataset.arff";
        public double trainSize = 0.1;
        public double testSize = 0.01;
        public int k = 3;
        public int time = 120;

        @Setup(Level.Trial)
        public void setup() {
            System.out.println("Carregando dados na memória! \n >> " + path);
            data = VirtualBlockFileLoader.fileLoader(path);
            System.out.println("Arquivo carregado! Número de instâncias: " + data.numInstances());
        }
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkSerialKnnProcessing(BenchmarkState state) throws Exception {
        SerialKnnProcessing.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkVirtualKnnProcessing(BenchmarkState state) throws Exception {
        VirtualKnnProcessing.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkPlatformKnnProcessing(BenchmarkState state) throws Exception {
        PlatformKnnProcessing.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkFutureKnnProcessing(BenchmarkState state) throws Exception {
        FutureKnnProcessing.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkForkAndJoinKnnProcessing(BenchmarkState state) throws Exception {
        ForkAndJoinKnn.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 1, time = 2)
    @Fork(1)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkAtomicKnnProcessing(BenchmarkState state) throws Exception {
        AtomicKnnProcessing.knnProcessing(state.data, state.data.numInstances(), state.trainSize, state.testSize, state.k, state.time);
    }
}

