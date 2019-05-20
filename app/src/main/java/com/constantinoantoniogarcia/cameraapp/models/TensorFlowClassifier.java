package com.constantinoantoniogarcia.cameraapp.models;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class TensorFlowClassifier implements Classifier {
    private String name; // name of the classifier
    private List<String> labels; // all possible predictions from the classifier
    private int inputSize;
    private int nChannels;
    private String inputName;
    private String outputName;
    private String[] outputNames;
    private float[] output;
    private TensorFlowInferenceInterface tfHelper;

    public static TensorFlowClassifier create(AssetManager assetManager, String name,
                                              String modelPath, String labelFile,
                                              int inputSize, int nChannels, String inputName, String outputName,
                                              boolean feedKeepProb) throws IOException {
        //intialize a classifier
        TensorFlowClassifier classifier = new TensorFlowClassifier();

        classifier.name = name;
        classifier.inputName = inputName;
        classifier.outputName = outputName;
        classifier.outputNames = new String[] {outputName};
        classifier.labels = readLabels(assetManager, labelFile);
        classifier.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        classifier.inputSize = inputSize;
        classifier.nChannels = nChannels;
        classifier.output = new float[1]; // 1 Since we use a Sigmoid classifier

        return classifier;
    }

    private static List<String> readLabels(AssetManager assetManager, String labelFile) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(labelFile)));

        String line;
        List<String> labels = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();
        return labels;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public ClassificationResult recognize(float[] pixels) {
        tfHelper.feed(inputName, pixels, 1, inputSize, inputSize, nChannels);

        tfHelper.run(outputNames);

        tfHelper.fetch(outputName, output);

        ClassificationResult ans = new ClassificationResult();
        String prediction = "";
        float probability = 0.0F;
        if (output[0] > 0.5) {
            prediction = labels.get(1);
            probability = output[0];
        } else {
            prediction = labels.get(0);
            probability = 1 - output[0];
        }
        Log.i("====> Classification", prediction + " with Pr = " + probability);
        ans.update(probability, prediction);

        return ans;
    }
}
