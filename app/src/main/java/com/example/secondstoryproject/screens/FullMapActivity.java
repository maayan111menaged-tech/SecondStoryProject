package com.example.secondstoryproject.screens;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.services.DatabaseService;

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

        AddAllMarkers();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    public void AddAllMarkers(){

        fullMap.getOverlays().clear();

        DatabaseService.getInstance().getDonationService()
                .getDonationsCountByCities(new DatabaseService.DatabaseCallback<java.util.HashMap<String, Integer>>() {

                    @Override
                    public void onCompleted(java.util.HashMap<String, Integer> cityCountMap) {

                        IsraelCity[] citys = IsraelCity.values();

                        for(int i = 0; i < citys.length; i++){
                            String cityName = citys[i].getHebrewName();
                            double lat = citys[i].getLatitude();
                            double lon = citys[i].getLongitude();

                            int count = cityCountMap.containsKey(cityName) ? cityCountMap.get(cityName) : 0;

                            addCityMarker(cityName, lat, lon, count);
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
    }
    private void addCityMarker(String city, double lat, double lon, int count) {
        Marker marker = new Marker(fullMap); //יצירת מרקר
        marker.setPosition(new GeoPoint(lat, lon)); // מיקום
        marker.setTitle(city + ": " + count + " תרומות"); // כיתוב
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // מרכוז וקביעה שהלמטה יגע במפה
        fullMap.getOverlays().add(marker); // הוספה למפה עצמה (לפי שכבות)
    }

    @Override public void onResume() { super.onResume(); fullMap.onResume(); }
    @Override public void onPause() { super.onPause(); fullMap.onPause(); }
}