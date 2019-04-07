package com.constantinoantoniogarcia.cameraapp.models;

public class ClassificationResult {
    private float probability;
    private String label;

    ClassificationResult() {
        this.probability = -1.0F;
        this.label = null;
    }

    public ClassificationResult(float probability, String label) {
        this.probability = probability;
        this.label = label;
    }

    void update(float probability, String label) {
        this.probability = probability;
        this.label = label;
    }

    public float getProbability() {
        return probability;
    }

    public String getLabel() {
        return label;
    }
}
