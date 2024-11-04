import atomic.AtomicKnnProcessing;
import atomic.AtomicFileLoader;
import callablefuture.FutureBlockFileLoader;
import callablefuture.FutureKnnProcessing;
import forkandjoin.ForkAndJoinKnn;
import platform.PlatformBlockFileLoader;
import platform.PlatformKnnProcessing;

import serial.SerialKnnProcessing;
import serial.SeriallFileLoader;
import virtual.VirtualBlockFileLoader;
import virtual.VirtualKnnProcessing;
import weka.core.Instances;


public class Main {
    public static void main(String[] args) throws Exception {
        // Carregar e pré-processar os dados
        String path = "/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/large_dataset.arff";
        System.out.println("Carregando dados na memória! \n >> " + path);

        final Instances data = VirtualBlockFileLoader.fileLoader(path);

        System.out.println("Arquivo carregado!" + data.numInstances());
        System.out.println("Iniciando processamento dos dados! \n >> ");

        double trainSize = 0.1;
        double testSize = 0.01;
        int k = 12;
        int time = 120;

        SerialKnnProcessing.knnProcessing(data, data.numInstances(), trainSize, testSize, k, time);
    }
}