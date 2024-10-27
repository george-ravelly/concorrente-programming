const { Worker, isMainThread, parentPort, workerData } = require('worker_threads');
const fs = require('fs');
const path = require('path');

const processarKnn = require('./KnnProcess');

const pathFile = path.resolve('/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/arquivoTest.arff');
const numWorkers = 4; // Número de workers que você quer usar

if (isMainThread) {
    const startTime = Date.now();
    console.log('Iniciando carregamento e pré-processamento!');
    // Thread principal
    const fileSize = fs.statSync(pathFile).size;
    const chunkSize = Math.ceil(fileSize / numWorkers); // Divide o arquivo em partes para cada worker
    const results = [];

    for (let i = 0; i < numWorkers; i++) {
        const start = i * chunkSize;
        const end = Math.min(start + chunkSize, fileSize);

        const worker = new Worker(__filename, {
            workerData: { pathFile, start, end }
        });

        worker.on('message', async (data) => {
            results.push(data);
            if (results.length === numWorkers) {
                console.log('Todos os workers terminaram.');
                console.log('Arquivo carregado em: ' + (Date.now() - startTime) + 'ms');
                await processarKnn(formatToArff(results.flat().join('').split('\n')));
            }
        });

        worker.on('error', (err) => {
            console.error(`Worker ${i} error:`, err);
        });

        worker.on('exit', (code) => {
            if (code !== 0) {
                console.error(`Worker ${i} saiu com código ${code}`);
            }
        });
    }
} else {
    // Código para cada worker
    const { pathFile, start, end } = workerData;
    const arquivo = [];

    const reader = fs.createReadStream(pathFile, {
        encoding: 'utf8',
        start: start,
        end: end,
        highWaterMark: 1024 * 1024
    });

    reader.on('data', (chunk) => {
        arquivo.push(chunk);
    });

    reader.on('end', () => {
        parentPort.postMessage(arquivo);
    });

    reader.on('error', (err) => {
        console.error('Erro na leitura:', err);
    });
}

function formatToArff (results = []) {
    // Define os atributos do dataset
    const attributes = ['feature1', 'feature2', 'feature3', 'label'];

    const dataset = {
        name: 'large_dataset',
        attributes,
        data: [],
    };

    results.forEach(line => {
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
    })
    return dataset;
}

