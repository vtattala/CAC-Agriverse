package com.example.plantdisease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private TextView welcomeText;
    private Button plantDiseaseBtn, insectDetectionBtn, chatbotBtn;
    private String userName, userRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Existing buttons
        Button plantInfoBtn = findViewById(R.id.plantInfoBtn);
        Button notepadBtn = findViewById(R.id.notepadBtn);

        // New Chatbot button
        chatbotBtn = findViewById(R.id.chatbotBtn);

        plantInfoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PlantInfoActivity.class);
            startActivity(intent);
        });

        notepadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotepadActivity.class);
            startActivity(intent);
        });

        // Get user info from previous activity
        Intent intent = getIntent();
        userName = intent.getStringExtra("USER_NAME");
        userRegion = intent.getStringExtra("USER_REGION");

        // Initialize UI elements
        welcomeText = findViewById(R.id.welcomeText);
        plantDiseaseBtn = findViewById(R.id.plantDiseaseBtn);
        insectDetectionBtn = findViewById(R.id.insectDetectionBtn);

        // Display welcome message
        welcomeText.setText("Welcome, " + userName + "!\nFrom: " + userRegion);

        // Plant Disease button
        plantDiseaseBtn.setOnClickListener(v -> {
            Intent plantIntent = new Intent(HomeActivity.this, MainActivity.class);
            plantIntent.putExtra("USER_NAME", userName);
            plantIntent.putExtra("USER_REGION", userRegion);
            startActivity(plantIntent);
        });

        // Insect Detection button
        insectDetectionBtn.setOnClickListener(v -> {
            Intent insectIntent = new Intent(HomeActivity.this, InsectActivity.class);
            insectIntent.putExtra("USER_NAME", userName);
            insectIntent.putExtra("USER_REGION", userRegion);
            startActivity(insectIntent);
        });

        // Chatbot button
        chatbotBtn.setOnClickListener(v -> {
            Intent chatIntent = new Intent(HomeActivity.this, ChatActivity.class);
            chatIntent.putExtra("USER_NAME", userName);
            chatIntent.putExtra("USER_REGION", userRegion);
            startActivity(chatIntent);
        });

//        forumBtn.setOnClickListener(v -> {
//            Intent forumIntent = new Intent(HomeActivity.this, QandAForum.class);
//            startActivity(forumIntent);
//        });
    }
}
