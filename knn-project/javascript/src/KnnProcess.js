const { Worker } = require('worker_threads')

const processarKnn = async ({data}) => {
    const startTime = Date.now();

    const process = async () => {

        // Dividir os dados em treino e teste (80% treino, 20% teste)
        const trainSize = Math.floor(data.length * 0.01);
        let shuffledData = data.sort(() => 0.5 - Math.random());

        let trainData = shuffledData.slice(0, trainSize);
        let testData = shuffledData.slice(trainSize, trainSize + Math.floor(trainSize * 0.01));

        console.log('train: ', trainSize, 'test: ', Math.floor(trainSize * 0.08))

        shuffledData = null;
        // Prepara os dados para o KNN
        const trainFeatures = trainData.map((d) => [d.feature1, d.feature2, d.feature3]);
        const trainLabels = trainData.map((d) => d.label);

        trainData = null;

        const testFeatures = testData.map((d) => [d.feature1, d.feature2, d.feature3]);
        const testLabels = testData.map((d) => d.label);

        testData = null;

        // Avaliação do modelo
        let correctPredictions = 0;

        const chunkSize = 100;
        let i = 1;
        let workerCount = 1;
        let start = 0;
        let end = chunkSize;

        console.log('Size: ',testFeatures.length)

        do {
            const chunkFeatures = testFeatures.slice(start, end);
            const chunkLabels = testLabels.slice(start, end);
            start = end;
            end += chunkSize;

            const worker = new Worker('/home/george/pessoal/Projetos/concurrent-programming/knn-project/javascript/src/MultiThreadProcessing.js', {
                workerData: {
                    trainFeatures,
                    trainLabels,
                    features: chunkFeatures,
                    labels: chunkLabels
                }
            })

            worker.on('message', (data) => {
                correctPredictions += data;
                if (++workerCount === i) {
                    const accuracy = (correctPredictions / testLabels.length) * 100;
                    console.log('Acuracia do modelo KNN: ' + accuracy + '%', 'Tempo total: ', (Date.now() - startTime) + 'ms');
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