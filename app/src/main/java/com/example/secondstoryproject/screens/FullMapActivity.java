package com.example.secondstoryproject.screens;

import android.os.Bundle;
import android.view.MotionEvent;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.views.CityInfoWindow;

public class FullMapActivity extends BaseActivity {

    private MapView fullMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        fullMap = findViewById(R.id.fullMap);
        fullMap.setTileSource(TileSourceFactory.MAPNIK);
        fullMap.setMultiTouchControls(true);
        fullMap.getController().setZoom(8.0);
        fullMap.getController().setCenter(new GeoPoint(31.5, 34.8));

        fullMap.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                InfoWindow.closeAllInfoWindowsOn(fullMap);
            }
            return false;
        });

        AddAllMarkers();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    public void AddAllMarkers() {
        fullMap.getOverlays().clear();

        DatabaseService.getInstance().getDonationService()
                .getDonationsCountByCities(DonationStatus.APPROVED_AVAILABLE,
                        new DatabaseService.DatabaseCallback<java.util.HashMap<String, Integer>>() {
                            @Override
                            public void onCompleted(java.util.HashMap<String, Integer> cityCountMap) {
                                IsraelCity[] cities = IsraelCity.values();
                                for (IsraelCity city : cities) {
                                    String cityName = city.getHebrewName();
                                    int count = cityCountMap.containsKey(cityName)
                                            ? cityCountMap.get(cityName) : 0;
                                    addCityMarker(cityName, city.getLatitude(),
                                            city.getLongitude(), count);
                                }
                                runOnUiThread(() -> fullMap.invalidate());
                            }

                            @Override
                            public void onFailed(Exception e) {}
                        });
    }

    private void addCityMarker(String city, double lat, double lon, int count) {
        Marker marker = new Marker(fullMap);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        CityInfoWindow infoWindow = new CityInfoWindow(fullMap, city, count);
        marker.setInfoWindow(infoWindow);

        marker.setOnMarkerClickListener((m, mapView) -> {
            InfoWindow.closeAllInfoWindowsOn(mapView);
            m.showInfoWindow();
            return true;
        });

        fullMap.getOverlays().add(marker);
    }

    @Override public void onResume() { super.onResume(); fullMap.onResume(); }
    @Override public void onPause() { super.onPause(); fullMap.onPause(); }
}