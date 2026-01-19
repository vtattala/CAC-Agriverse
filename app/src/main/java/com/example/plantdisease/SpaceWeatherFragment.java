package com.example.plantdisease;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpaceWeatherFragment extends Fragment {

    private TextView kpIndexValue, kpStatus, solarWindValue, solarWindStatus;
    private TextView f107Value, f107Status, geomagneticStatus, radiationStatus;
    private TextView cropImpact, satelliteImpact, aiSummary;

    private final OkHttpClient httpClient = new OkHttpClient();

    // store last values for AI prompt
    private double lastKpIndex = Double.NaN;
    private double lastEstimatedSpeed = Double.NaN;
    private double lastF107 = Double.NaN;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_space_weather, container, false);

        // Connect to XML views
        kpIndexValue = view.findViewById(R.id.kpIndexValue);
        kpStatus = view.findViewById(R.id.kpStatus);
        solarWindValue = view.findViewById(R.id.solarWindValue);
        solarWindStatus = view.findViewById(R.id.solarWindStatus);
        f107Value = view.findViewById(R.id.f107Value);
        f107Status = view.findViewById(R.id.f107Status);
        geomagneticStatus = view.findViewById(R.id.geomagneticStatus);
        radiationStatus = view.findViewById(R.id.radiationStatus);

        // Agriculture impact views
        cropImpact = view.findViewById(R.id.cropImpact);
        satelliteImpact = view.findViewById(R.id.satelliteImpact);

        // AI summary view
        aiSummary = view.findViewById(R.id.aiSummary);

        // Refresh button
        view.findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");
            cropImpact.setText("Analyzing crop impact...");
            satelliteImpact.setText("Analyzing satellite impact...");
            aiSummary.setText("Generating AI summary...");
            fetchSpaceWeather();
        });

        // Fetch data on load
        fetchSpaceWeather();

        return view;
    }

    private void fetchSpaceWeather() {
        String spaceWeatherUrl = "https://services.swpc.noaa.gov/json/planetary_k_index_1m.json";
        Request request = new Request.Builder().url(spaceWeatherUrl).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("SPACE_WEATHER", "Failed to fetch Kp index", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        kpIndexValue.setText("Error");
                        kpStatus.setText("Unable to fetch data");
                        cropImpact.setText("Unable to determine crop impact");
                        aiSummary.setText("AI summary unavailable.");
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                parseSpaceWeather(body);
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
                Log.e("SOLAR_WIND", "Failed to fetch solar wind", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        satelliteImpact.setText("Unable to determine satellite impact");
                        aiSummary.setText("AI summary unavailable.");
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                parseSolarWind(body);
            }
        });
    }

    private void parseSpaceWeather(String json) {
        try {
            if (json == null || json.isEmpty()) {
                throw new Exception("Empty Kp JSON");
            }

            JSONArray array = new JSONArray(json);

            if (array.length() > 0 && getActivity() != null) {
                JSONObject latest = array.getJSONObject(array.length() - 1);
                double kpIndex = latest.optDouble("kp_index", Double.NaN);
                lastKpIndex = kpIndex;

                getActivity().runOnUiThread(() -> {
                    kpIndexValue.setText(Double.isNaN(kpIndex) ? "--" : String.format("%.1f", kpIndex));

                    // GEOMAGNETIC STATUS + COLORS
                    if (Double.isNaN(kpIndex)) {
                        kpStatus.setText("N/A");
                        kpStatus.setTextColor(Color.parseColor("#7B83A6"));
                        geomagneticStatus.setText("Data unavailable");
                        geomagneticStatus.setTextColor(Color.parseColor("#7B83A6"));
                        cropImpact.setText("Crop Impact: Data unavailable");
                        cropImpact.setTextColor(Color.parseColor("#7B83A6"));
                    } else if (kpIndex < 4) {
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

                    // RADIATION IMPACT
                    if (!Double.isNaN(kpIndex) && kpIndex < 5) {
                        radiationStatus.setText("Normal radiation levels");
                        radiationStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else if (!Double.isNaN(kpIndex)) {
                        radiationStatus.setText("Elevated radiation — monitor crop stress");
                        radiationStatus.setTextColor(Color.parseColor("#D84315"));
                    }
                });
            }
        } catch (Exception e) {
            Log.e("PARSE_KP", "Error parsing Kp JSON", e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> aiSummary.setText("AI summary unavailable."));
            }
        }
    }

    private void parseSolarWind(String json) {
        try {
            if (json == null || json.isEmpty()) {
                throw new Exception("Empty solar wind JSON");
            }

            JSONArray array = new JSONArray(json);

            if (array.length() > 0 && getActivity() != null) {
                JSONObject latest = array.getJSONObject(array.length() - 1);

                double bt = latest.optDouble("bt", 0.0);
                double estimatedSpeed = 300 + (bt * 50);
                double f107 = 70 + (bt * 10);

                // store for AI prompt
                lastEstimatedSpeed = estimatedSpeed;
                lastF107 = f107;

                getActivity().runOnUiThread(() -> {
                    solarWindValue.setText(String.format("%.0f km/s", estimatedSpeed));

                    // SOLAR WIND STATUS
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

                    // F10.7 SOLAR FLUX
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

                    // After both datasets are updated, generate AI summary
                    String prompt = buildAIPrompt(
                            lastKpIndex,
                            lastEstimatedSpeed,
                            lastF107,
                            cropImpact.getText().toString(),
                            satelliteImpact.getText().toString()
                    );
                    generateAISummary(prompt);
                });
            }
        } catch (Exception e) {
            Log.e("PARSE_SOLAR", "Error parsing solar wind JSON", e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> aiSummary.setText("AI summary unavailable."));
            }
        }
    }

    // Build a concise prompt for the AI service
    private String buildAIPrompt(double kp, double wind, double f107, String cropImpactText, String satelliteImpactText) {
        return "You are an agricultural advisor. Based on the following space weather data, generate a short 2-3 sentence summary explaining how today's solar conditions affect crops, vegetation indices (NDVI), and satellite-based monitoring. Use plain language for farmers.\n\n" +
                "Kp Index: " + (Double.isNaN(kp) ? "N/A" : String.format("%.1f", kp)) + "\n" +
                "Solar Wind Speed: " + (Double.isNaN(wind) ? "N/A" : String.format("%.0f km/s", wind)) + "\n" +
                "F10.7 Solar Flux: " + (Double.isNaN(f107) ? "N/A" : String.format("%.0f sfu", f107)) + "\n" +
                "Crop Impact Note: " + cropImpactText + "\n" +
                "Satellite Impact Note: " + satelliteImpactText + "\n\n" +
                "Provide a clear, actionable summary for farmers (2-3 sentences).";
    }

    // Robust AI call that tolerates multiple response shapes and logs raw response
    private void generateAISummary(String prompt) {
        if (getActivity() == null) return;

        aiSummary.setText("Generating AI summary...");

        // Replace with your backend endpoint. For emulator testing use: http://10.0.2.2:3000/generate
        String aiUrl = "https://your-ai-endpoint.example.com/generate";

        OkHttpClient clientWithTimeout = httpClient.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        try {
            JSONObject payload = new JSONObject();
            payload.put("prompt", prompt);
            payload.put("max_tokens", 120);

            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(aiUrl)
                    .post(body)
                    .header("Accept", "application/json")
                    .build();

            clientWithTimeout.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("AI_SUMMARY", "Network failure", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                aiSummary.setText("AI summary unavailable (network).")
                        );
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String raw = null;
                    try {
                        int code = response.code();
                        Log.d("AI_SUMMARY", "HTTP status: " + code);
                        if (response.body() != null) raw = response.body().string();
                        Log.d("AI_SUMMARY", "Raw response: " + raw);

                        if (code >= 400) {
                            Log.e("AI_SUMMARY", "Server error code: " + code);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> aiSummary.setText("AI service error: " + code));
                            }
                            return;
                        }

                        if (raw == null || raw.isEmpty()) {
                            throw new IOException("Empty response body");
                        }

                        // Try to extract summary from common shapes
                        String summary = null;
                        try {
                            JSONObject obj = new JSONObject(raw);

                            if (obj.has("summary")) summary = obj.optString("summary", null);
                            if (summary == null && obj.has("text")) summary = obj.optString("text", null);

                            if (summary == null && obj.has("choices")) {
                                JSONArray choices = obj.optJSONArray("choices");
                                if (choices != null && choices.length() > 0) {
                                    JSONObject first = choices.optJSONObject(0);
                                    if (first != null) summary = first.optString("text", first.optString("message", null));
                                }
                            }

                            if (summary == null && obj.has("result")) summary = obj.optString("result", null);
                            if (summary == null && obj.has("data")) summary = obj.optString("data", null);
                        } catch (Exception parseEx) {
                            Log.w("AI_SUMMARY", "Response not JSON or unexpected format", parseEx);
                        }

                        if (summary == null) {
                            // fallback: use raw body (trim)
                            summary = raw.length() > 1200 ? raw.substring(0, 1200) + "..." : raw;
                        }

                        final String finalSummary = summary;
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> aiSummary.setText(finalSummary));
                        }
                    } catch (Exception e) {
                        Log.e("AI_SUMMARY", "Error processing AI response", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> aiSummary.setText("AI summary error."));
                        }
                    } finally {
                        response.close();
                    }
                }
            });

        } catch (Exception e) {
            Log.e("AI_SUMMARY", "Failed to build request", e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> aiSummary.setText("AI summary error."));
            }
        }
    }
}
