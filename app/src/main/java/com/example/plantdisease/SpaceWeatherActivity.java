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

    private TextView kpIndexValue, kpStatus, solarWindValue, solarWindStatus;
    private TextView f107Value, f107Status, geomagneticStatus, radiationStatus;
    private TextView cropImpact, satelliteImpact, aiSummary;

    private final OkHttpClient httpClient = new OkHttpClient();

    private double lastKpIndex = Double.NaN;
    private double lastEstimatedSpeed = Double.NaN;
    private double lastF107 = Double.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_space_weather);

        kpIndexValue = findViewById(R.id.kpIndexValue);
        kpStatus = findViewById(R.id.kpStatus);
        solarWindValue = findViewById(R.id.solarWindValue);
        solarWindStatus = findViewById(R.id.solarWindStatus);
        f107Value = findViewById(R.id.f107Value);
        f107Status = findViewById(R.id.f107Status);
        geomagneticStatus = findViewById(R.id.geomagneticStatus);
        radiationStatus = findViewById(R.id.radiationStatus);

        cropImpact = findViewById(R.id.cropImpact);
        satelliteImpact = findViewById(R.id.satelliteImpact);
        aiSummary = findViewById(R.id.aiSummary);

        findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");
            cropImpact.setText("Analyzing crop impact...");
            satelliteImpact.setText("Analyzing satellite impact...");
            aiSummary.setText("Generating AI summary...");
            fetchSpaceWeather();
        });

        fetchSpaceWeather();
    }

    private void fetchSpaceWeather() {
        String kpUrl = "https://services.swpc.noaa.gov/json/planetary_k_index_1m.json";

        Request request = new Request.Builder().url(kpUrl).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    kpStatus.setText("Error fetching data");
                    cropImpact.setText("Unable to determine crop impact");
                    aiSummary.setText("AI summary unavailable.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseSpaceWeather(response.body().string());
            }
        });

        fetchSolarWind();
    }

    private void fetchSolarWind() {
        String url = "https://services.swpc.noaa.gov/json/rtsw/rtsw_mag_1m.json";

        Request request = new Request.Builder().url(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SOLAR_WIND", "Failed", e);
                runOnUiThread(() -> {
                    satelliteImpact.setText("Unable to determine satellite impact");
                    aiSummary.setText("AI summary unavailable.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseSolarWind(response.body().string());
            }
        });
    }

    private void parseSpaceWeather(String json) {
        try {
            JSONArray array = new JSONArray(json);
            JSONObject latest = array.getJSONObject(array.length() - 1);

            double kpIndex = latest.getDouble("kp_index");
            lastKpIndex = kpIndex;

            runOnUiThread(() -> {
                kpIndexValue.setText(String.format("%.1f", kpIndex));

                if (kpIndex < 4) {
                    kpStatus.setText("Quiet");
                    kpStatus.setTextColor(Color.parseColor("#2E7D32"));
                    geomagneticStatus.setText("Normal geomagnetic conditions");
                    geomagneticStatus.setTextColor(Color.parseColor("#2E7D32"));

                    cropImpact.setText("Crop Impact: Satellite vegetation data is reliable today.");
                    cropImpact.setTextColor(Color.parseColor("#2E7D32"));

                } else if (kpIndex < 6) {
                    kpStatus.setText("Unsettled");
                    kpStatus.setTextColor(Color.parseColor("#F9A825"));
                    geomagneticStatus.setText("Minor geomagnetic disturbance");
                    geomagneticStatus.setTextColor(Color.parseColor("#F9A825"));

                    cropImpact.setText("Crop Impact: NDVI and soil moisture readings may show slight noise.");
                    cropImpact.setTextColor(Color.parseColor("#F9A825"));

                } else {
                    kpStatus.setText("Storm");
                    kpStatus.setTextColor(Color.parseColor("#C62828"));
                    geomagneticStatus.setText("Strong geomagnetic storm");
                    geomagneticStatus.setTextColor(Color.parseColor("#C62828"));

                    cropImpact.setText("Crop Impact: Strong storm — vegetation indices may be inaccurate.");
                    cropImpact.setTextColor(Color.parseColor("#C62828"));
                }

                if (kpIndex < 5) {
                    radiationStatus.setText("Normal radiation levels");
                    radiationStatus.setTextColor(Color.parseColor("#2E7D32"));
                } else {
                    radiationStatus.setText("Elevated radiation — monitor crop stress");
                    radiationStatus.setTextColor(Color.parseColor("#D84315"));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> aiSummary.setText("AI summary unavailable."));
        }
    }

    private void parseSolarWind(String json) {
        try {
            JSONArray array = new JSONArray(json);
            JSONObject latest = array.getJSONObject(array.length() - 1);

            double bt = latest.optDouble("bt", 0.0);
            double estimatedSpeed = 300 + (bt * 50);
            double f107 = 70 + (bt * 10);

            lastEstimatedSpeed = estimatedSpeed;
            lastF107 = f107;

            runOnUiThread(() -> {
                solarWindValue.setText(String.format("%.0f km/s", estimatedSpeed));

                if (estimatedSpeed < 400) {
                    solarWindStatus.setText("Slow");
                    solarWindStatus.setTextColor(Color.parseColor("#2E7D32"));

                    satelliteImpact.setText("Satellite Impact: Stable conditions for crop monitoring.");
                    satelliteImpact.setTextColor(Color.parseColor("#2E7D32"));

                } else if (estimatedSpeed < 600) {
                    solarWindStatus.setText("Elevated");
                    solarWindStatus.setTextColor(Color.parseColor("#F9A825"));

                    satelliteImpact.setText("Satellite Impact: Minor interference possible in NDVI data.");
                    satelliteImpact.setTextColor(Color.parseColor("#F9A825"));

                } else {
                    solarWindStatus.setText("High-Speed Stream");
                    solarWindStatus.setTextColor(Color.parseColor("#D84315"));

                    satelliteImpact.setText("Satellite Impact: Expect reduced accuracy in vegetation data.");
                    satelliteImpact.setTextColor(Color.parseColor("#D84315"));
                }

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

                // Build prompt and call AI
                String prompt = buildAIPrompt(
                        lastKpIndex,
                        lastEstimatedSpeed,
                        lastF107,
                        cropImpact.getText().toString(),
                        satelliteImpact.getText().toString()
                );
                generateAISummary(prompt);
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> aiSummary.setText("AI summary unavailable."));
        }
    }

    private String buildAIPrompt(double kp, double wind, double f107, String cropImpactText, String satelliteImpactText) {
        return "You are an agricultural advisor. Based on the following space weather data, generate a short 2-3 sentence summary explaining how today's solar conditions affect crops, vegetation indices (NDVI), and satellite-based monitoring. Use plain language for farmers.\n\n" +
                "Kp Index: " + (Double.isNaN(kp) ? "N/A" : String.format("%.1f", kp)) + "\n" +
                "Solar Wind Speed: " + (Double.isNaN(wind) ? "N/A" : String.format("%.0f km/s", wind)) + "\n" +
                "F10.7 Solar Flux: " + (Double.isNaN(f107) ? "N/A" : String.format("%.0f sfu", f107)) + "\n" +
                "Crop Impact Note: " + cropImpactText + "\n" +
                "Satellite Impact Note: " + satelliteImpactText + "\n\n" +
                "Provide a clear, actionable summary for farmers (2-3 sentences).";
    }

    private void generateAISummary(String prompt) {
        aiSummary.setText("Generating AI summary...");

        String aiUrl = "https://your-ai-endpoint.example.com/generate"; // <-- replace with your endpoint

        try {
            JSONObject payload = new JSONObject();
            payload.put("prompt", prompt);
            payload.put("max_tokens", 120);

            Request request = new Request.Builder()
                    .url(aiUrl)
                    .post(okhttp3.RequestBody.create(
                            payload.toString(),
                            okhttp3.MediaType.parse("application/json")
                    ))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> aiSummary.setText("AI summary unavailable."));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String body = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(body);
                        String summary = obj.optString("summary", null);
                        if (summary == null) summary = obj.optString("text", body);

                        final String finalSummary = summary;
                        runOnUiThread(() -> aiSummary.setText(finalSummary));
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> aiSummary.setText("AI summary error."));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            aiSummary.setText("AI summary error.");
        }
    }
}
