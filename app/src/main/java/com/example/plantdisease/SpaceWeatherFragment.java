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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpaceWeatherFragment extends Fragment {

    private TextView kpIndexValue, kpStatus, solarWindValue, solarWindStatus;
    private TextView f107Value, f107Status, geomagneticStatus, radiationStatus;
    private TextView cropImpact, satelliteImpact;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        // NEW AGRICULTURE IMPACT TEXTVIEWS
        cropImpact = view.findViewById(R.id.cropImpact);
        satelliteImpact = view.findViewById(R.id.satelliteImpact);

        // Refresh button
        view.findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");
            cropImpact.setText("Analyzing crop impact...");
            satelliteImpact.setText("Analyzing satellite impact...");
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        kpIndexValue.setText("Error");
                        kpStatus.setText("Unable to fetch data");
                        cropImpact.setText("Unable to determine crop impact");
                    });
                }
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> satelliteImpact.setText("Unable to determine satellite impact"));
                }
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

            if (array.length() > 0 && getActivity() != null) {
                JSONObject latest = array.getJSONObject(array.length() - 1);
                double kpIndex = latest.getDouble("kp_index");

                getActivity().runOnUiThread(() -> {
                    kpIndexValue.setText(String.format("%.1f", kpIndex));

                    // -----------------------------
                    // GEOMAGNETIC STATUS + COLORS
                    // -----------------------------
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

                    // -----------------------------
                    // RADIATION IMPACT
                    // -----------------------------
                    if (kpIndex < 5) {
                        radiationStatus.setText("Normal radiation levels");
                        radiationStatus.setTextColor(Color.parseColor("#2E7D32"));
                    } else {
                        radiationStatus.setText("Elevated radiation — monitor crop stress");
                        radiationStatus.setTextColor(Color.parseColor("#D84315"));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseSolarWind(String json) {
        try {
            JSONArray array = new JSONArray(json);

            if (array.length() > 0 && getActivity() != null) {
                JSONObject latest = array.getJSONObject(array.length() - 1);

                double bt = latest.getDouble("bt");
                double estimatedSpeed = 300 + (bt * 50);
                double f107 = 70 + (bt * 10);

                getActivity().runOnUiThread(() -> {
                    solarWindValue.setText(String.format("%.0f km/s", estimatedSpeed));

                    // -----------------------------
                    // SOLAR WIND STATUS
                    // -----------------------------
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

                    // -----------------------------
                    // F10.7 SOLAR FLUX
                    // -----------------------------
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
        }
    }
}
