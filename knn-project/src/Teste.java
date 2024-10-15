import java.util.concurrent.atomic.AtomicReference;

public class Teste {
    public static void main(String[] args) {
        AtomicReference<String> teste = new AtomicReference<>("vazio!");
        Runnable r = () -> {
            teste.set("cheio");
        };

        var builder = Thread.ofVirtual().name("teste", 1).start(r);

        System.out.println(teste);
    }
}
