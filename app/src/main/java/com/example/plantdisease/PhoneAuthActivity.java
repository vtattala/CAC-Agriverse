package com.example.plantdisease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneAuthActivity extends AppCompatActivity {

    private EditText phoneInput;
    private Button continueBtn;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth);

        // Link UI elements
        phoneInput = findViewById(R.id.phoneInput);
        continueBtn = findViewById(R.id.continueBtn);
        backButton = findViewById(R.id.backButton);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            finish(); // Go back to WelcomeActivity
        });

        // Continue button click
        continueBtn.setOnClickListener(v -> {
            String phoneNumber = phoneInput.getText().toString().trim();

            // Validate phone number
            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phoneNumber.length() < 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Implement Firebase Phone Authentication
            // For now, show a toast and proceed to HomeActivity
            Toast.makeText(this, "Verifying phone number...", Toast.LENGTH_SHORT).show();

            // Navigate to HomeActivity after verification
            continueBtn.postDelayed(() -> {
                Intent intent = new Intent(PhoneAuthActivity.this, MainContainerActivity.class);
                startActivity(intent);
                finish();

            }, 1000);
        });
    }

    // TODO: Add Firebase Phone Authentication
    /*
     * To implement Firebase Phone Auth:
     * 1. Add Firebase to your project
     * 2. Add dependency: implementation 'com.google.firebase:firebase-auth'
     * 3. Use PhoneAuthProvider to send verification code
     * 4. Create OTP verification screen
     * 5. Verify code and sign in user
     */
}