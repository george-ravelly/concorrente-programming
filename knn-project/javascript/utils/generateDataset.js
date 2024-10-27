const fs = require('fs');
const path = require('path');

const generateLargeArff = (filePath, targetSizeInMB) => {
    const stream = fs.createWriteStream(filePath, { flags: 'w' });
    const estimatedLineSize = 50; // Estimativa do tamanho de cada linha (em bytes)
    const targetSizeInBytes = targetSizeInMB * 1024 * 1024;
    const totalLines = Math.floor(targetSizeInBytes / estimatedLineSize);

    console.log(`Generating ARFF file of approximately ${targetSizeInMB} MB...`);

    // Escreve o cabeçalho do ARFF
    stream.write('@RELATION large_dataset\n\n');
    stream.write('@ATTRIBUTE feature1 NUMERIC\n');
    stream.write('@ATTRIBUTE feature2 NUMERIC\n');
    stream.write('@ATTRIBUTE feature3 NUMERIC\n');
    stream.write('@ATTRIBUTE label {0,1}\n\n');
    stream.write('@DATA\n');

    // Escreve as instâncias de dados
    for (let i = 0; i < totalLines; i++) {
        const feature1 = (Math.random() * 100).toFixed(2);
        const feature2 = (Math.random() * 50).toFixed(2);
        const feature3 = (Math.random() * 200 - 100).toFixed(2);
        const label = Math.random() < 0.5 ? 0 : 1;

        // Adiciona uma linha ao ARFF
        stream.write(`${feature1},${feature2},${feature3},${label}\n`);

        // Exibe progresso a cada 100 mil linhas geradas
        if (i % 100000 === 0) {
            console.log(`Progress: ${(i / totalLines * 100).toFixed(2)}%`);
        }
    }

    stream.end(() => {
        console.log('ARFF file generation completed.');
    });
};

// Especifica o caminho e tamanho do arquivo (em MB)
generateLargeArff(path.join(__dirname, 'arquivoTest.arff'), 100);