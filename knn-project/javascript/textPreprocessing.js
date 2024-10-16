const fs = require('fs');
const readline = require('readline');

const textPreprocessing = async (filePath) => {
    // Define os atributos do dataset
    const attributes = ['polarity', 'title', 'text'];
    const classValues = ['1', '2'];

    const dataset = {
        name: 'reviews',
        attributes,
        data: [],
    };

    // Função para processar cada linha do arquivo
    const processLine = (line) => {
        // Remove aspas duplas e divide por vírgulas
        const values = line.replace(/"/g, '').split(',');

        if (values.length === 3) {
            const instance = {
                polarity: values[0], // Class label (1 ou 2)
                title: values[1],    // Título da análise
                text: values[2],     // Corpo da análise
            };
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