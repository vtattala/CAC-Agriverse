package com.example.plantdisease;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private TextView resultText;
    private ProgressBar progressBar;
    private Button cameraBtn, galleryBtn;
    private Bitmap selectedBitmap;
    private DiseaseModel diseaseModel;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get user info from welcome screen
        Intent intent = getIntent();
        String userName = intent.getStringExtra("USER_NAME");
        String userRegion = intent.getStringExtra("USER_REGION");

        if (userName != null && userRegion != null) {
            // You can display it somewhere or use it later
            android.util.Log.i(TAG, "User: " + userName + " from " + userRegion);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "MainActivity onCreate");

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        progressBar = findViewById(R.id.progressBar);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);

        // Load model on background thread
        resultText.setText("Loading ML model...");
        new Thread(() -> {
            try {
                Log.i(TAG, "Loading DiseaseModel on thread...");
                diseaseModel = new DiseaseModel(MainActivity.this);
                runOnUiThread(() -> {
                    Toast.makeText(this, "✓ Model loaded!", Toast.LENGTH_LONG).show();
                    resultText.setText("Ready! Select an image to analyze");
                    Log.i(TAG, "Model loaded successfully");
                });
            } catch (Exception e) {
                Log.e(TAG, "Model loading failed", e);
                runOnUiThread(() -> {
                    String errorMsg = "❌ Model Error: " + e.getMessage();
                    resultText.setText(errorMsg);
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        }).start();

        requestPermissions();

        cameraBtn.setOnClickListener(v -> openCamera());
        galleryBtn.setOnClickListener(v -> openGallery());
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permissions granted");
            } else {
                Log.w(TAG, "Permissions denied");
            }
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedBitmap = (Bitmap) result.getData().getExtras().get("data");
                            imageView.setImageBitmap(selectedBitmap);
                            identifyDisease();
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
                                identifyDisease();
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

    private void identifyDisease() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (diseaseModel == null) {
            Toast.makeText(this, "❌ Model not loaded yet", Toast.LENGTH_LONG).show();
            resultText.setText("❌ Model failed to load. Please restart app.");
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        resultText.setText("🔍 Analyzing plant...");
        Log.i(TAG, "Starting prediction...");

        new Thread(() -> {
            try {
                DiseaseModel.Prediction prediction = diseaseModel.predictDisease(selectedBitmap);

                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (prediction != null) {
                        resultText.setText(
                                "Disease: " + prediction.diseaseName + "\n\n" +
                                        "Confidence: " + String.format("%.2f%%", prediction.confidence * 100)
                        );
                        Log.i(TAG, "Prediction successful");
                    } else {
                        resultText.setText("❌ Prediction failed");
                        Toast.makeText(MainActivity.this, "Could not analyze image", Toast.LENGTH_SHORT).show();
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
        if (diseaseModel != null) {
            diseaseModel.close();
        }
    }
}