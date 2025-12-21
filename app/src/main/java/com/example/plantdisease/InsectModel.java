package com.example.plantdisease;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InsectModel {
    private Interpreter tflite;
    private List<String> insectLabels;
    private JsonObject insectFacts;
    private static final int IMG_SIZE = 224;
    private static final int NUM_THREADS = 4;
    private static final String TAG = "InsectModel";

    public static class InsectPrediction {
        public String insectName;
        public float confidence;
        public String fact1;
        public String fact2;

        public InsectPrediction(String name, float conf, String f1, String f2) {
            insectName = name;
            confidence = conf;
            fact1 = f1;
            fact2 = f2;
        }
    }

    public InsectModel(Context context) throws Exception {
        try {
            Log.i(TAG, "Loading insect model...");

            // Load labels and facts
            loadLabelsAndFacts(context);
            Log.i(TAG, "Labels loaded: " + insectLabels.size() + " classes");

            // Load TFLite model
            MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "insect_model.tflite");
            Log.i(TAG, "Model buffer loaded");

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(NUM_THREADS);

            tflite = new Interpreter(tfliteModel, options);
            Log.i(TAG, "✓ Insect model loaded!");

        } catch (Exception e) {
            Log.e(TAG, "Error loading model", e);
            throw new Exception("Insect model failed: " + e.getMessage(), e);
        }
    }

    private void loadLabelsAndFacts(Context context) throws IOException {
        insectLabels = new ArrayList<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("insect_labels.json"))
        );
        StringBuilder jsonString = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        reader.close();

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonString.toString(), JsonObject.class);


        String[] classesArray = gson.fromJson(
                jsonObject.getAsJsonArray("classes"),
                String[].class
        );

        for (String className : classesArray) {
            insectLabels.add(className);
        }


        insectFacts = jsonObject.getAsJsonObject("facts");

        Log.i(TAG, "Loaded " + insectLabels.size() + " insect types");
    }

    public InsectPrediction predictInsect(Bitmap bitmap) {
        if (bitmap == null || tflite == null || insectLabels == null) {
            Log.e(TAG, "Prediction failed: missing components");
            return null;
        }

        try {
            Log.i(TAG, "Processing insect image...");


            TensorImage image = new TensorImage();
            image.load(bitmap);

            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(0f, 255f))
                    .build();

            TensorImage processedImage = imageProcessor.process(image);

            float[][] output = new float[1][insectLabels.size()];
            tflite.run(processedImage.getBuffer(), output);


            float maxConfidence = 0;
            int maxIndex = 0;
            for (int i = 0; i < output[0].length; i++) {
                if (output[0][i] > maxConfidence) {
                    maxConfidence = output[0][i];
                    maxIndex = i;
                }
            }

            String insectName = insectLabels.get(maxIndex);

            String fact1 = "No information available";
            String fact2 = "No information available";

            if (insectFacts.has(insectName)) {
                JsonArray factsArray = insectFacts.getAsJsonArray(insectName);
                if (factsArray.size() >= 2) {
                    fact1 = factsArray.get(0).getAsString();
                    fact2 = factsArray.get(1).getAsString();
                }
            }

            Log.i(TAG, "Prediction: " + insectName + " (" + (maxConfidence * 100) + "%)");

            return new InsectPrediction(insectName, maxConfidence, fact1, fact2);

        } catch (Exception e) {
            Log.e(TAG, "Prediction error", e);
            return null;
        }
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
            Log.i(TAG, "Model closed");
        }
    }
}
