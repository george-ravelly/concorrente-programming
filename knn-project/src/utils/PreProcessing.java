package utils;

import weka.attributeSelection.PrincipalComponents;
import weka.core.Instance;
import weka.core.Instances;

public class PreProcessing {
    public static Instances pca(Instances data) throws Exception {

        // Verifica se há instâncias nulas e remove-as
        for (int i = 0; i < data.numInstances(); i++) {
            if (data.instance(i) == null) {
                System.out.println(data.get(i));
                data.delete(i);
            }
        }

        PrincipalComponents pca = new PrincipalComponents();
        pca.setCenterData(true);
        pca.setVarianceCovered(0.95);

        pca.buildEvaluator(data);

        return pca.transformedData(data);
    }
}
