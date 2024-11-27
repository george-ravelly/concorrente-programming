const Benchmark = require('benchmark');
const path = require('path');

const textPreprocessing = require('./AsyncFileReader.js');
const processarKnn = require('./KnnProcess');

// Configurar Benchmark
const suite = new Benchmark.Suite();

const pathFile = path.resolve('/home/george/pessoal/Projetos/concurrent-programming/knn-project/src/main/resourses/large_dataset.arff');


suite
    .add('Leitura do arquivo', {
        defer: true, // Indica que a função é assíncrona
        fn: function (deferred) {
            textPreprocessing(pathFile).then(() => {
                deferred.resolve();
            }).catch((err) => {
                console.error('Erro na leitura do arquivo:', err);
                deferred.resolve();
            });
        }
    })
    .add('Processamento do KNN', {
        defer: true, // Indica que a função é assíncrona
        fn: function (deferred) {
            textPreprocessing(pathFile).then(processarKnn).then(() => {
                deferred.resolve();
            }).catch((err) => {
                console.error('Erro:', err);
                deferred.resolve();
            });
        },
    })
    .on('cycle', function (event) {
        console.log(String(event.target));
    })
    .on('complete', function () {
        // Quando a execução estiver completa, exibe o tempo médio de execução
        this.forEach(function (bench) {
            console.log(`${bench.name} - Tempo médio: ${bench.stats.mean} ms`);
        });
    })
    .run({ async: true });