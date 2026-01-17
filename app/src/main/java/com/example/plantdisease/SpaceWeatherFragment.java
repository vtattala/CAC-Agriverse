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

        // Refresh button
        view.findViewById(R.id.refreshBtn).setOnClickListener(v -> {
            kpIndexValue.setText("Loading...");
            solarWindValue.setText("Loading...");
            f107Value.setText("Loading...");
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
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                parseSpaceWeather(json);
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
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String json = response.body().string();
                parseSolarWind(json);
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
        }
    }

    private void parseSolarWind(String json) {
        try {
            JSONArray array = new JSONArray(json);

            if (array.length() > 0 && getActivity() != null) {
                JSONObject latest = array.getJSONObject(array.length() - 1);
                double bt = latest.getDouble("bt");
                double estimatedSpeed = 300 + (bt * 50);

                getActivity().runOnUiThread(() -> {
                    solarWindValue.setText(String.format("%.0f km/s", estimatedSpeed));

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
        }
    }
}