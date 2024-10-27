const { parentPort, workerData } = require('worker_threads')
const KNN = require("ml-knn");

const { trainFeatures, trainLabels, features, labels } = workerData;

const knn = new KNN(trainFeatures, trainLabels, { k: 5 });

const prediction = knn.predict(features);
let countPrediction = 0;
for (let i = 0; i < labels.length; i++) {
    if (labels[i] === prediction[i]) countPrediction++;
}
parentPort.postMessage(countPrediction);
