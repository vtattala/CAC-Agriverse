package com.example.plantdisease;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private TextView welcomeText;
    private String userName, userEmail, userCountry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the home layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Get user data from bundle
        if (getArguments() != null) {
            userName = getArguments().getString("USER_NAME");
            userEmail = getArguments().getString("USER_EMAIL");
            userCountry = getArguments().getString("USER_COUNTRY");
        }

        // Find all views
        welcomeText = view.findViewById(R.id.welcomeText);
        View plantDiseaseBtn = view.findViewById(R.id.plantDiseaseBtn);
        View insectDetectionBtn = view.findViewById(R.id.insectDetectionBtn);
        View plantInfoBtn = view.findViewById(R.id.plantInfoBtn);
        View notepadBtn = view.findViewById(R.id.notepadBtn);
        View encyclopediaBtn = view.findViewById(R.id.encyclopediaBtn);
        View regionalGuideBtn = view.findViewById(R.id.regionalGuideBtn);
        View chatbotBtn = view.findViewById(R.id.chatbotBtn);
        View satelliteBtn = view.findViewById(R.id.satelliteBtn);

        // Display welcome message
        welcomeText.setText(
                "Welcome back, " + userName +
                        "!\nCountry: " + userCountry +
                        "\nEmail: " + userEmail
        );

        // Plant Disease button
        plantDiseaseBtn.setOnClickListener(v -> {
            Intent plantIntent = new Intent(getActivity(), MainActivity.class);
            plantIntent.putExtra("USER_NAME", userName);
            plantIntent.putExtra("USER_EMAIL", userEmail);
            plantIntent.putExtra("USER_COUNTRY", userCountry);
            startActivity(plantIntent);
        });

        // Insect Detection button
        insectDetectionBtn.setOnClickListener(v -> {
            Intent insectIntent = new Intent(getActivity(), InsectActivity.class);
            insectIntent.putExtra("USER_NAME", userName);
            insectIntent.putExtra("USER_EMAIL", userEmail);
            insectIntent.putExtra("USER_COUNTRY", userCountry);
            startActivity(insectIntent);
        });

        // Encyclopedia button
        encyclopediaBtn.setOnClickListener(v -> {
            Intent encyclopediaIntent = new Intent(getActivity(), EncyclopediaActivity.class);
            startActivity(encyclopediaIntent);
        });

        // Regional Guide button
        regionalGuideBtn.setOnClickListener(v -> {
            Intent regionalIntent = new Intent(getActivity(), RegionalGuideActivity.class);
            regionalIntent.putExtra("USER_COUNTRY", userCountry);
            startActivity(regionalIntent);
        });

        // Plant Info button
        plantInfoBtn.setOnClickListener(v -> {
            Intent plantInfoIntent = new Intent(getActivity(), PlantInfoActivity.class);
            startActivity(plantInfoIntent);
        });

        // Notepad button
        notepadBtn.setOnClickListener(v -> {
            Intent notepadIntent = new Intent(getActivity(), NotepadActivity.class);
            startActivity(notepadIntent);
        });

        // Chatbot button
        chatbotBtn.setOnClickListener(v -> {
            Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
            chatIntent.putExtra("USER_NAME", userName);
            startActivity(chatIntent);
        });

        // Agricultural Data (Satellite) button
        satelliteBtn.setOnClickListener(v -> {
            Intent satIntent = new Intent(getActivity(), SatelliteActivity.class);
            satIntent.putExtra("USER_COUNTRY", userCountry);
            startActivity(satIntent);
        });

        return view;
    }
}