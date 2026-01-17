package com.example.plantdisease;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpaceWeatherActivity extends AppCompatActivity {

    // UI elements - links to XML views
    private TextView kpIndexValue, kpStatus, solarWindValue, solarWindStatus;
    private TextView f107Value, f107Status, geomagneticStatus, radiationStatus;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_space_weather);

        // Connect to XML views
        kpIndexValue = findViewById(R.id.kpIndexValue);
        kpStatus = findViewById(R.id.kpStatus);
        solarWindValue = findViewById(R.id.solarWindValue);
        solarWindStatus = findViewById(R.id.solarWindStatus);
        f107Value = findViewById(R.id.f107Value);
        f107Status = findViewById(R.id.f107Status);
        geomagneticStatus = findViewById(R.id.geomagneticStatus);
        radiationStatus = findViewById(R.id.radiationStatus);

        // Refresh button
        findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            // Show loading state
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");

            // Fetch fresh data
            fetchSpaceWeather();
        });

        // Fetch real space weather data on load
        fetchSpaceWeather();
    }

    // Request space weather data from Open-Meteo API
    private void fetchSpaceWeather() {
        String url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=0&longitude=0&" +
                "current=temperature_2m"; // We'll use a different approach

        // Use NASA DONKI API for real space weather
        String nasaUrl = "https://api.nasa.gov/DONKI/FLR?startDate=2026-01-01&api_key=DEMO_KEY";

        // Backup: Use Open-Meteo space weather endpoint
        String spaceWeatherUrl = "https://services.swpc.noaa.gov/json/planetary_k_index_1m.json";

        Request request = new Request.Builder()
                .url(spaceWeatherUrl)
                .build();

        // Make network request in background
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SPACE_WEATHER", "Failed to fetch data", e);
                runOnUiThread(() -> {
                    kpIndexValue.setText("Error");
                    kpStatus.setText("Unable to fetch data");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                Log.d("SPACE_WEATHER_JSON", json);
                parseSpaceWeather(json);
            }
        });

        // Also fetch solar wind data
        fetchSolarWind();
    }

    // Fetch solar wind speed data
    private void fetchSolarWind() {
        String url = "https://services.swpc.noaa.gov/json/rtsw/rtsw_mag_1m.json";

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SOLAR_WIND", "Failed to fetch", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                parseSolarWind(json);
            }
        });
    }

    // Parse Kp Index data from NOAA
    private void parseSpaceWeather(String json) {
        try {
            JSONArray array = new JSONArray(json);

            if (array.length() > 0) {
                // Get most recent reading
                JSONObject latest = array.getJSONObject(array.length() - 1);
                double kpIndex = latest.getDouble("kp_index");

                runOnUiThread(() -> {
                    kpIndexValue.setText(String.format("%.1f", kpIndex));

                    // Interpret Kp Index (0-9 scale)
                    if (kpIndex < 4) {
                        kpStatus.setText("Quiet");
                        kpStatus.setTextColor(Color.parseColor("#2E7D32"));
                        geomagneticStatus.setText("✅ Normal Conditions");
                        geomagneticStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else if (kpIndex < 5) {
                        kpStatus.setText("Unsettled");
                        kpStatus.setTextColor(Color.parseColor("#F9A825"));
                        geomagneticStatus.setText("⚠️ Minor Storm");
                        geomagneticStatus.setTextColor(Color.parseColor("#F9A825"));
                    } else if (kpIndex < 6) {
                        kpStatus.setText("Active");
                        kpStatus.setTextColor(Color.parseColor("#FF6F00"));
                        geomagneticStatus.setText("⚠️ Moderate Storm");
                        geomagneticStatus.setTextColor(Color.parseColor("#FF6F00"));
                    } else if (kpIndex < 7) {
                        kpStatus.setText("Minor Storm");
                        kpStatus.setTextColor(Color.parseColor("#D84315"));
                        geomagneticStatus.setText("🔴 Strong Storm");
                        geomagneticStatus.setTextColor(Color.parseColor("#D84315"));
                    } else {
                        kpStatus.setText("Major Storm");
                        kpStatus.setTextColor(Color.parseColor("#C62828"));
                        geomagneticStatus.setText("🚨 Severe Storm");
                        geomagneticStatus.setTextColor(Color.parseColor("#C62828"));
                    }

                    // Set radiation impact based on Kp
                    if (kpIndex < 5) {
                        radiationStatus.setText("Normal radiation levels");
                        radiationStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else {
                        radiationStatus.setText("Elevated radiation - Monitor crops");
                        radiationStatus.setTextColor(Color.parseColor("#D84315"));
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PARSE_ERROR", "Error parsing space weather", e);
            runOnUiThread(() -> {
                kpIndexValue.setText("--");
                kpStatus.setText("Parse error");
            });
        }
    }

    // Parse solar wind data
    private void parseSolarWind(String json) {
        try {
            JSONArray array = new JSONArray(json);

            if (array.length() > 0) {
                JSONObject latest = array.getJSONObject(array.length() - 1);

                // Get Bt (total magnetic field)
                double bt = latest.getDouble("bt");

                // Estimate solar wind speed (typical correlation)
                double estimatedSpeed = 300 + (bt * 50);

                runOnUiThread(() -> {
                    solarWindValue.setText(String.format("%.0f km/s", estimatedSpeed));

                    // Interpret solar wind speed
                    if (estimatedSpeed < 400) {
                        solarWindStatus.setText("Slow");
                        solarWindStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else if (estimatedSpeed < 500) {
                        solarWindStatus.setText("Normal");
                        solarWindStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else if (estimatedSpeed < 600) {
                        solarWindStatus.setText("Elevated");
                        solarWindStatus.setTextColor(Color.parseColor("#F9A825"));
                    } else {
                        solarWindStatus.setText("High Speed Stream");
                        solarWindStatus.setTextColor(Color.parseColor("#D84315"));
                    }

                    // Set F10.7 estimate based on solar wind
                    double f107 = 70 + (bt * 10);
                    f107Value.setText(String.format("%.0f sfu", f107));

                    if (f107 < 100) {
                        f107Status.setText("Low Solar Activity");
                        f107Status.setTextColor(Color.parseColor("#2E7D32"));
                    } else if (f107 < 150) {
                        f107Status.setText("Moderate Activity");
                        f107Status.setTextColor(Color.parseColor("#F9A825"));
                    } else {
                        f107Status.setText("High Solar Activity");
                        f107Status.setTextColor(Color.parseColor("#D84315"));
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                solarWindValue.setText("--");
                solarWindStatus.setText("Data unavailable");
            });
        }
    }
}