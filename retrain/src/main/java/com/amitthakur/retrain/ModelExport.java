package com.amitthakur.retrain;

public class ModelExport {
    public double[][] weights;  // [classes][features]
    public double[] bias;       // [classes]

    public ModelExport(double[][] w, double[] b) {
        this.weights = w;
        this.bias    = b;
    }
}
