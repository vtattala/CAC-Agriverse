package com.example.plantdisease;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegionalGuideActivity extends AppCompatActivity {

    private static final String TAG = "RegionalGuide";
    private static final String WEATHER_API_KEY = "1cb1a88483037ec05c04e9eee9bd5521";

    private EditText locationInput;
    private Button searchBtn, backBtn;
    private TextView resultText;
    private ImageView mapView;
    private ProgressBar progressBar;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regional_guide);

        locationInput = findViewById(R.id.locationInput);
        searchBtn = findViewById(R.id.searchBtn);
        backBtn = findViewById(R.id.backBtn);
        resultText = findViewById(R.id.resultText);
        mapView = findViewById(R.id.mapView);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);

        searchBtn.setOnClickListener(v -> searchRegion());
        backBtn.setOnClickListener(v -> finish());
    }

    private void searchRegion() {
        String location = locationInput.getText().toString().trim();

        if (location.isEmpty()) {
            Toast.makeText(this, "Enter a city or region", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        resultText.setText("🌍 Analyzing agricultural conditions for " + location + "...");

        new Thread(() -> {
            try {
                RegionalData data = fetchRegionalData(location);

                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (data != null) {
                        displayRegionalInfo(data);
                        loadMapImage(data.latitude, data.longitude);
                    } else {
                        resultText.setText("❌ Location not found\n\nTry:\n• Major cities (New York, Tokyo)\n• Regions (California, Tuscany)\n• Check spelling");
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    resultText.setText("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private RegionalData fetchRegionalData(String location) {
        try {
            String encodedLocation = location.replace(" ", "%20");
            String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    encodedLocation + "&appid=" + WEATHER_API_KEY + "&units=metric";

            String response = makeRequest(weatherUrl);
            if (response == null) return null;

            JsonObject json = new Gson().fromJson(response, JsonObject.class);

            RegionalData data = new RegionalData();
            data.location = json.get("name").getAsString();
            data.country = json.getAsJsonObject("sys").get("country").getAsString();

            JsonObject coord = json.getAsJsonObject("coord");
            data.latitude = coord.get("lat").getAsDouble();
            data.longitude = coord.get("lon").getAsDouble();

            JsonObject main = json.getAsJsonObject("main");
            data.temperature = main.get("temp").getAsDouble();
            data.humidity = main.get("humidity").getAsInt();

            JsonArray weatherArray = json.getAsJsonArray("weather");
            data.weatherDescription = weatherArray.get(0).getAsJsonObject().get("description").getAsString();

            determineClimateAndPlants(data);

            Log.i(TAG, "Found: " + data.location);
            return data;

        } catch (Exception e) {
            Log.e(TAG, "API error", e);
            return null;
        }
    }

    private void determineClimateAndPlants(RegionalData data) {
        double lat = Math.abs(data.latitude);

        if (lat < 23.5) {
            data.climateZone = "Tropical";
            data.idealVegetables = "Tomatoes, Peppers, Eggplant, Okra, Sweet Potato";
            data.idealGrains = "Rice, Corn, Millet, Sorghum";
            data.idealFruits = "Mango, Banana, Papaya, Pineapple";
            data.wateringFrequency = "Daily watering recommended, high rainfall region";
            data.growingSeason = "Year-round growing possible";
        } else if (lat < 35) {
            data.climateZone = "Subtropical";
            data.idealVegetables = "Tomatoes, Cucumbers, Squash, Beans, Carrots";
            data.idealGrains = "Wheat, Barley, Oats, Corn";
            data.idealFruits = "Citrus (Oranges, Lemons), Figs, Grapes, Peaches";
            data.wateringFrequency = "Every 2-3 days, moderate rainfall";
            data.growingSeason = "March-October (8-9 months)";
        } else if (lat < 50) {
            data.climateZone = "Temperate";
            data.idealVegetables = "Potatoes, Cabbage, Lettuce, Peas, Carrots, Beets";
            data.idealGrains = "Wheat, Rye, Barley, Oats";
            data.idealFruits = "Apples, Pears, Berries, Cherries";
            data.wateringFrequency = "Every 3-5 days, variable rainfall";
            data.growingSeason = "April-September (5-6 months)";
        } else {
            data.climateZone = "Cold/Subarctic";
            data.idealVegetables = "Hardy vegetables: Kale, Cabbage, Root vegetables";
            data.idealGrains = "Hardy wheat, Barley, Rye";
            data.idealFruits = "Berries, Cold-hardy apples";
            data.wateringFrequency = "Every 4-7 days, short season";
            data.growingSeason = "May-August (3-4 months)";
        }

        if (data.temperature < 10) {
            data.currentAdvice = "⚠️ Currently too cold for most planting. Prepare soil and plan for spring.";
        } else if (data.temperature > 30) {
            data.currentAdvice = "☀️ High heat - ensure adequate irrigation and mulching to retain moisture.";
        } else {
            data.currentAdvice = "✅ Good temperatures for active growing season. Monitor soil moisture.";
        }
    }

    private String makeRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) return null;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "Request error", e);
            return null;
        }
    }

    private void displayRegionalInfo(RegionalData data) {
        StringBuilder result = new StringBuilder();

        result.append("📍 ").append(data.location.toUpperCase()).append(", ").append(data.country).append("\n");
        result.append("═══════════════════════════════\n\n");

        result.append("🌡️ CURRENT CONDITIONS\n");
        result.append("Temperature: ").append(String.format("%.1f°C", data.temperature)).append("\n");
        result.append("Weather: ").append(capitalize(data.weatherDescription)).append("\n");
        result.append("Humidity: ").append(data.humidity).append("%\n");
        result.append("Coordinates: ").append(String.format("%.2f, %.2f", data.latitude, data.longitude)).append("\n\n");

        result.append("🌍 CLIMATE ZONE\n");
        result.append(data.climateZone).append("\n");
        result.append("Growing Season: ").append(data.growingSeason).append("\n\n");

        result.append("🥬 IDEAL VEGETABLES\n");
        result.append(data.idealVegetables).append("\n\n");

        result.append("🌾 IDEAL GRAINS\n");
        result.append(data.idealGrains).append("\n\n");

        result.append("🍎 IDEAL FRUITS\n");
        result.append(data.idealFruits).append("\n\n");

        result.append("💧 WATERING GUIDE\n");
        result.append(data.wateringFrequency).append("\n\n");

        result.append("📅 CURRENT ADVICE\n");
        result.append(data.currentAdvice).append("\n\n");

        result.append("─────────────────────────\n");
        result.append("🌐 Source: OpenWeatherMap + Agricultural Database\n");
        result.append("Updated: ").append(new SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(new Date()));

        resultText.setText(result.toString());
        scrollView.smoothScrollTo(0, 0);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void loadMapImage(double latitude, double longitude) {
        new Thread(() -> {
            try {
                // Using OpenStreetMap tile server directly
                int zoom = 12;
                double x = Math.floor((longitude + 180) / 360 * (1 << zoom));
                double y = Math.floor((1 - Math.log(Math.tan(Math.toRadians(latitude)) +
                        1 / Math.cos(Math.toRadians(latitude))) / Math.PI) / 2 * (1 << zoom));

                String mapUrl = String.format(Locale.US,
                        "https://tile.openstreetmap.org/%d/%d/%d.png",
                        zoom, (int)x, (int)y
                );

                Log.i(TAG, "Loading map from: " + mapUrl);

                URL url = new URL(mapUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "AcreIntelligence/1.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                int responseCode = conn.getResponseCode();
                Log.i(TAG, "Response code: " + responseCode);

                if (responseCode == 200) {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(
                            conn.getInputStream()
                    );

                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            mapView.setImageBitmap(bitmap);
                            mapView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Log.i(TAG, "✓ Map loaded successfully!");
                            Toast.makeText(this, "Map loaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Bitmap is null");
                            Toast.makeText(this, "Map image failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Map load error: " + e.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + e.getMessage());
                e.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Map unavailable: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private static class RegionalData {
        String location;
        String country;
        double latitude;
        double longitude;
        double temperature;
        int humidity;
        String weatherDescription;
        String climateZone;
        String idealVegetables;
        String idealGrains;
        String idealFruits;
        String wateringFrequency;
        String growingSeason;
        String currentAdvice;
    }
}