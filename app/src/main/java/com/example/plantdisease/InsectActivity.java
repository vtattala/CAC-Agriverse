package com.example.plantdisease;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class InsectActivity extends AppCompatActivity {

    private static final String TAG = "InsectActivity";
    private ImageView imageView;
    private TextView resultText;
    private ProgressBar progressBar;
    private Button cameraBtn, galleryBtn, backBtn;
    private Bitmap selectedBitmap;
    private InsectModel insectModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insect);

        Log.i(TAG, "InsectActivity onCreate");

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        progressBar = findViewById(R.id.progressBar);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        backBtn = findViewById(R.id.backBtn);

        // Load model
        resultText.setText("Loading insect model...");
        new Thread(() -> {
            try {
                Log.i(TAG, "Loading InsectModel...");
                insectModel = new InsectModel(InsectActivity.this);
                runOnUiThread(() -> {
                    Toast.makeText(this, "✓ Insect model loaded!", Toast.LENGTH_LONG).show();
                    resultText.setText("Ready! Capture or select an insect image");
                    Log.i(TAG, "Model loaded successfully");
                });
            } catch (Exception e) {
                Log.e(TAG, "Model loading failed", e);
                runOnUiThread(() -> {
                    String errorMsg = "❌ Model Error: " + e.getMessage();
                    resultText.setText(errorMsg);
                    Toast.makeText(InsectActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        }).start();

        cameraBtn.setOnClickListener(v -> openCamera());
        galleryBtn.setOnClickListener(v -> openGallery());
        backBtn.setOnClickListener(v -> finish());
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedBitmap = (Bitmap) result.getData().getExtras().get("data");
                            imageView.setImageBitmap(selectedBitmap);
                            identifyInsect();
                        }
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                                imageView.setImageBitmap(selectedBitmap);
                                identifyInsect();
                            } catch (IOException e) {
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void identifyInsect() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (insectModel == null) {
            Toast.makeText(this, "❌ Model not loaded yet", Toast.LENGTH_LONG).show();
            resultText.setText("❌ Model failed to load. Please restart.");
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        resultText.setText("🔍 Identifying insect...");
        Log.i(TAG, "Starting prediction...");

        new Thread(() -> {
            try {
                InsectModel.InsectPrediction prediction = insectModel.predictInsect(selectedBitmap);

                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (prediction != null) {
                        String result = "Insect: " + prediction.insectName + "\n\n" +
                                "Confidence: " + String.format("%.2f%%", prediction.confidence * 100) + "\n\n" +
                                "Impact on Crops:\n" +
                                "• " + prediction.fact1 + "\n" +
                                "• " + prediction.fact2;
                        resultText.setText(result);
                        Log.i(TAG, "Prediction successful");
                    } else {
                        resultText.setText("❌ Prediction failed");
                        Toast.makeText(InsectActivity.this, "Could not analyze image", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Prediction exception", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    resultText.setText("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (insectModel != null) {
            insectModel.close();
        }
    }
}