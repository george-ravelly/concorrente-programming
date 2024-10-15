import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class ConversorCsvToArff {
    public static void main(String[] args) {
        // Define os caminhos dos arquivos CSV e ARFF
        Runnable executor = () -> {
            Path csvFilePath = Path.of("/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/train.csv");
            Path arffFilePath = Path.of("/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/arquivoTrain.arff");

            // Padrão regex para dividir as colunas respeitando as aspas duplas
            Pattern pattern = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            try (BufferedReader reader = Files.newBufferedReader(csvFilePath);
                 BufferedWriter writer = Files.newBufferedWriter(arffFilePath)) {

                // Escreve o cabeçalho do arquivo ARFF
                writer.write("@relation reviews");
                writer.newLine();
                writer.newLine();

                writer.write("@attribute polarity {1, 2}"); // Classe de polaridade com valores fixos 1 (negativo) e 2 (positivo)
                writer.newLine();
                writer.write("@attribute title STRING");
                writer.newLine();
                writer.write("@attribute text STRING");
                writer.newLine();
                writer.newLine();
                writer.write("@data");
                writer.newLine();

                // Processa as linhas de dados
                String line;
                while ((line = reader.readLine()) != null) {
                    // Usa o padrão regex para dividir a linha em colunas (respeitando as aspas duplas)
                    String[] values = pattern.split(line, -1);
                    if (values.length == 3) {
                        String polarity = values[0].trim();
                        String title = values[1].trim();
                        String text = values[2].trim();

                        // Remove aspas ao redor do título e texto, e converte "\n" para real quebra de linha
                        polarity = polarity.replace("\"", "");
                        title = title.replaceAll("^\"|\"$", "").replace("\"\"", "\"").replace("\\n", "\n");
                        text = text.replaceAll("^\"|\"$", "").replace("\"\"", "\"").replace("\\n", "\n");
                        title = processString(title);
                        text = processString(text);

                        // Formata os campos para o formato ARFF
                        if (title.contains("\"") || text.contains("\"")) continue;
                        writer.write(polarity + ",\"" + title + "\",\""+ text + "\"");
                        writer.newLine();
                    }
                }

                System.out.println("Conversão concluída: " + arffFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        var builder = Thread.ofPlatform().name("converter", 1).start(executor);
        while (builder.isAlive()) {
            System.out.println("convertendo!!!");
        }
    }

    public static String processString(String input) {
        // Remove todas as aspas duplas da string
        // Adiciona aspas duplas no início e no final
        return input.replaceAll("\"", "'").replace(":-\\", "");
    }
}

