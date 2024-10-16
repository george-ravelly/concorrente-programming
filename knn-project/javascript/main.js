const natural = require('natural');
const KNN = require('ml-knn');
const textPreprocessing = require('./textPreprocessing'); // Supondo que esteja no mesmo diretório

(async () => {
    const startTime = Date.now();
    console.log('Iniciando carregamento e pré-processamento!');

    // Carregar e pré-processar os dados usando a função TextPreprocessing
    const { data } = await textPreprocessing('/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/arquivoTest.arff');
    console.log('Arquivo carregado em: ' + (Date.now() - startTime) + 'ms');

    const process = async () => {
        // Pré-processamento de texto: Transformação de texto em vetores
        const tfidf = new natural.TfIdf();
        const beforeTfidf = data.map((row) => {
            const { polarity, title, text } = row;
            tfidf.addDocument(text);
            return { polarity: parseInt(polarity), title, text };
        });

        // Itera sobre os documentos já adicionados e calcula os vetores TF-IDF
        const transformedData = beforeTfidf.map((row, index) => {
            const { polarity, title, text } = row;

            // Calcula o vetor TF-IDF para cada documento
            const vector = [];
            tfidf.listTerms(index).forEach((term) => {
                vector.push(term.tfidf);
            });

            return {
                polarity,
                title,
                text,
                vector,
            };
        });

        // Dividir os dados em treino e teste (80% treino, 20% teste)
        const trainSize = Math.floor(transformedData.length * 0.8);
        const shuffledData = transformedData.sort(() => 0.5 - Math.random());

        const trainData = shuffledData.slice(0, trainSize);
        const testData = shuffledData.slice(trainSize);

        // Prepara os dados para o KNN
        const trainFeatures = trainData.map((d) => d.vector);
        const trainLabels = trainData.map((d) => d.polarity);

        const testFeatures = testData.map((d) => d.vector);
        const testLabels = testData.map((d) => d.polarity);

        // Criar e treinar o modelo KNN
        const knn = new KNN(trainFeatures, trainLabels, { k: 5 });

        // Avaliação do modelo
        let correctPredictions = 0;
        testFeatures.forEach((features, index) => {
            const prediction = knn.predict(features);
            if (prediction === testLabels[index]) {
                correctPredictions++;
            }
        });

        const accuracy = (correctPredictions / testLabels.length) * 100;
        console.log('Acuracia do modelo KNN: ' + accuracy + '%');
    };

    console.log('Iniciando processamento: ' + (Date.now() - startTime) + 'ms');
    await process();

    console.log('Finalizado! Tempo total: ' + (Date.now() - startTime) + 'ms');
})();