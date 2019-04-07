package com.constantinoantoniogarcia.cameraapp.models;

public interface Classifier {
    String getName();

    ClassificationResult recognize(final float[] pixels);

}
