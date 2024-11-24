package main.ufrn.app.utils;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.PrincipalComponents;

public class PreprocessData {
    public static Instances applyPCA(Instances data, int numComponents) throws Exception {
        PrincipalComponents pca = new PrincipalComponents();
        pca.setMaximumAttributes(numComponents);
        pca.setInputFormat(data);
        return Filter.useFilter(data, pca);
    }
}
