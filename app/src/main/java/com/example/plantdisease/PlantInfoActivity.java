package com.example.plantdisease;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PlantInfoActivity extends AppCompatActivity {

    private EditText searchInput;
    private Button searchBtn, backBtn;
    private TextView resultText;
    private ScrollView scrollView;
    private JsonObject plantDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_info);

        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        backBtn = findViewById(R.id.backBtn);
        resultText = findViewById(R.id.resultText);
        scrollView = findViewById(R.id.scrollView);

        // Load plant database
        loadPlantDatabase();

        searchBtn.setOnClickListener(v -> searchPlant());
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadPlantDatabase() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("plant_info.json"))
            );
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            Gson gson = new Gson();
            plantDatabase = gson.fromJson(jsonString.toString(), JsonObject.class);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading plant database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void searchPlant() {
        String query = searchInput.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a plant name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Search for plant (case-insensitive)
        String plantKey = findPlantKey(query);

        if (plantKey != null && plantDatabase.has(plantKey)) {
            JsonObject plantInfo = plantDatabase.getAsJsonObject(plantKey);
            displayPlantInfo(plantKey, plantInfo);
        } else {
            resultText.setText("❌ Plant not found.\n\nTry: Tomato, Corn, Wheat, Rice, Potato, Soybean, Apple, Grape, Strawberry, Pepper, Lettuce, Carrot");
        }
    }

    private String findPlantKey(String query) {
        String queryLower = query.toLowerCase();

        // Exact match
        for (String key : plantDatabase.keySet()) {
            if (key.toLowerCase().equals(queryLower)) {
                return key;
            }
        }

        // Partial match
        for (String key : plantDatabase.keySet()) {
            if (key.toLowerCase().contains(queryLower) || queryLower.contains(key.toLowerCase())) {
                return key;
            }
        }

        return null;
    }

    private void displayPlantInfo(String plantName, JsonObject info) {
        StringBuilder result = new StringBuilder();

        result.append("🌱 ").append(plantName.toUpperCase()).append("\n");
        result.append("═══════════════════════════════\n\n");

        result.append("📋 Scientific Name:\n");
        result.append(info.get("scientific_name").getAsString()).append("\n\n");

        result.append("🌾 Planting Season:\n");
        result.append(info.get("planting_season").getAsString()).append("\n\n");

        result.append("🌍 Soil Requirements:\n");
        result.append(info.get("soil_requirements").getAsString()).append("\n\n");

        result.append("💧 Watering:\n");
        result.append(info.get("watering").getAsString()).append("\n\n");

        result.append("☀️ Sunlight:\n");
        result.append(info.get("sunlight").getAsString()).append("\n\n");

        result.append("📏 Spacing:\n");
        result.append(info.get("spacing").getAsString()).append("\n\n");

        result.append("🧪 Fertilizer:\n");
        result.append(info.get("fertilizer").getAsString()).append("\n\n");

        result.append("⏰ Harvest Time:\n");
        result.append(info.get("harvest_time").getAsString()).append("\n\n");

        result.append("🐛 Common Pests:\n");
        result.append(info.get("common_pests").getAsString()).append("\n\n");

        result.append("🦠 Diseases:\n");
        result.append(info.get("diseases").getAsString()).append("\n\n");

        result.append("💡 Farming Tips:\n");
        result.append(info.get("tips").getAsString()).append("\n");

        resultText.setText(result.toString());
        scrollView.smoothScrollTo(0, 0);
    }
}