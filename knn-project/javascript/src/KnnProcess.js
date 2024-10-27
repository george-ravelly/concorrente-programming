const KNN = require('ml-knn');
const { Worker, isMainThread, parentPort, workerData } = require('worker_threads')

const processarKnn = async ({data}) => {
    const startTime = Date.now();

    const process = async () => {

        // Dividir os dados em treino e teste (80% treino, 20% teste)
        const trainSize = Math.floor(data.length * 0.1);
        let shuffledData = data.sort(() => 0.5 - Math.random());

        let trainData = shuffledData.slice(0, trainSize);
        let testData = shuffledData.slice(trainSize,trainSize + trainSize * 0.08);

        shuffledData = null;
        // Prepara os dados para o KNN
        const trainFeatures = trainData.map((d) => [d.feature1, d.feature2, d.feature3]);
        const trainLabels = trainData.map((d) => d.label);

        trainData = null;

        const testFeatures = testData.map((d) => [d.feature1, d.feature2, d.feature3]);
        const testLabels = testData.map((d) => d.label);

        testData = null;

        // Criar e treinar o modelo KNN

        // Avaliação do modelo
        let correctPredictions = 0;

        const chunkSize = 1000;
        let i = 1;
        let workerCount = 1;
        let start = 0;
        let end = chunkSize;

        do {
            const chunkFeatures = testFeatures.slice(start, end);
            const chunkLabels = testLabels.slice(start, end);
            start = end;
            end += chunkSize;

            const worker = new Worker('./MultiThreadProcessing.js', {
                workerData: {
                    trainFeatures,
                    trainLabels,
                    features: chunkFeatures,
                    labels: chunkLabels
                }
            })

            worker.on('message', (data) => {
                correctPredictions += data;
                console.log(correctPredictions)
                if (++workerCount === i) {
                    const accuracy = (correctPredictions / testLabels.length) * 100;
                    console.log('Acuracia do modelo KNN: ' + accuracy + '%');
                }
            })

            worker.on('error', (err) => {
                console.error(`Worker ${i} error:`, err);
            });

            worker.on('exit', (code) => {
                if (code !== 0) {
                    console.error(`Worker ${i} saiu com código ${code}`);
                }
            });
            i++;
        } while (end < testFeatures.length);
    };

    console.log('Iniciando processamento: ' + (Date.now() - startTime) + 'ms');
    await process();

    console.log('Finalizado! Tempo total: ' + (Date.now() - startTime) + 'ms');
}

module.exports = processarKnn;