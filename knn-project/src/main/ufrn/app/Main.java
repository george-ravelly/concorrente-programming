package main.ufrn.app;

import main.ufrn.app.platform.PlatformKnnProcessing;

import main.ufrn.app.serial.SerialKnnProcessing;
import main.ufrn.app.serial.SeriallFileLoader;
import main.ufrn.app.virtual.VirtualBlockFileLoader;
import weka.core.Instances;


public class Main {
    public static void main(String[] args) throws Exception {
//        org.openjdk.jmh.main.ufrn.app.Main.main(args);
        // Carregar e pré-processar os dados
        String path = "/home/george/pessoal/Projetos/concurrent-programming/knn-project/src/main/resourses/large_dataset.arff";
        System.out.println("Carregando dados na memória! \n >> " + path);


        final Instances data = SeriallFileLoader.fileLoader(path);

        System.out.println("Arquivo carregado!" + data.numInstances());
        System.out.println("Iniciando processamento dos dados! \n >> ");

        double trainSize = 0.1;
        double testSize = 0.01;
        int k = 3;
        int time = 120;

        SerialKnnProcessing.knnProcessing(data, data.numInstances(), trainSize, testSize, k, time);
    }
}