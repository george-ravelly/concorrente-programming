const fs = require('fs');
const readline = require('readline');
const textPreprocessing = async (filePath) => {
    // Define os atributos do dataset
    const attributes = ['feature1', 'feature2', 'feature3', 'label'];

    const dataset = {
        name: 'large_dataset',
        attributes,
        data: [],
    };
    // Função para processar cada linha do arquivo
    const processLine = (line) => {
        // Remove aspas duplas e divide por vírgulas
        const values = line.split(',');

        if (values.length === 4) {
            const instance = {
                feature1: parseFloat(values[0]),
                feature2: parseFloat(values[1]),
                feature3: parseFloat(values[2]),
                label: parseFloat(values[3])
            }
            dataset.data.push(instance);
        }
    };
    // Cria uma interface de leitura de linhas
    const fileStream = fs.createReadStream(filePath);
    const rl = readline.createInterface({
        input: fileStream,
        crlfDelay: Infinity,
    });
    console.log('Carregando dataset...');
    // Usando async/await para processar o arquivo linha por linha
    for await (const line of rl) {
        processLine(line);
    }
    console.log(`Dataset carregado com ${dataset.data.length} instâncias.`);
    return dataset;
}
module.exports = textPreprocessing;