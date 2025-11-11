package com.example.plantdisease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private EditText nameInput, regionInput;
    private Button continueBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Link UI elements
        nameInput = findViewById(R.id.nameInput);
        regionInput = findViewById(R.id.regionInput);
        continueBtn = findViewById(R.id.continueBtn);

        // Continue button click
        continueBtn.setOnClickListener(v -> {
            String userName = nameInput.getText().toString().trim();
            String userRegion = regionInput.getText().toString().trim();

            // Validate inputs
            if (userName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userRegion.isEmpty()) {
                Toast.makeText(this, "Please enter your region", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show welcome message
            Toast.makeText(this,
                    "Welcome to AgriVerse, " + userName + " from " + userRegion + "!",
                    Toast.LENGTH_LONG).show();

            // Go to main activity after 1 second
            // Inside WelcomeActivity.java, replace this section:

            continueBtn.postDelayed(() -> {
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);  // Changed from MainActivity
                intent.putExtra("USER_NAME", userName);
                intent.putExtra("USER_REGION", userRegion);
                startActivity(intent);
                finish();
            }, 1500);
        });
    }
}