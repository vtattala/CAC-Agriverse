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
    private TextView cropImpact, satelliteImpact;

    private final OkHttpClient httpClient = new OkHttpClient();

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

        findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");
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
                runOnUiThread(() -> kpStatus.setText("Error fetching data"));
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

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
        }
    }

    private void parseSolarWind(String json) {
        try {
            JSONArray array = new JSONArray(json);
            JSONObject latest = array.getJSONObject(array.length() - 1);

            double bt = latest.getDouble("bt");
            double estimatedSpeed = 300 + (bt * 50);
            double f107 = 70 + (bt * 10);

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
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
