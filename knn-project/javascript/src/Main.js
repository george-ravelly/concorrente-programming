const path = require('path');
const fileLoader = require('./AsyncFileReader.js');

const knnProcessing = require('./KnnProcess');

const pathFile = path.resolve('/home/george/pessoal/Projetos/concurrent-programming/knn-project/src/main/resources/large_dataset.arff');


console.log('Iniciando carregamento e pré-processamento!');
(async () => await knnProcessing(await fileLoader(pathFile)))();