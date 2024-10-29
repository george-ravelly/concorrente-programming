const path = require('path');
const textPreprocessing = require('./AsyncFileReader.js');

const processarKnn = require('./KnnProcess');

const pathFile = path.resolve('/home/george/pessoal/Projetos/concurrent-programming/knn-project/resourse/large_dataset.arff');


console.log('Iniciando carregamento e pré-processamento!');
(async () => await processarKnn(await textPreprocessing(pathFile)))();