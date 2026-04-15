package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.core.view.GravityCompat;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.models.UserLevel;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.utils.SharedPreferencesUtil;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import androidx.preference.PreferenceManager;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    ProgressBar rateProgressBar;
    ImageView currentRateIcon, nextRateIcon;
    private MapView miniMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav.setSelectedItemId(R.id.menu_home);

        rateProgressBar = findViewById(R.id.rateProgressBar);
        currentRateIcon = findViewById(R.id.currentRateIcon);
        nextRateIcon = findViewById(R.id.nextRateIcon);

        User currentUser = SharedPreferencesUtil.getUser(this);
        updateUserLevelUI(currentUser);

        // ── מפה קטנה ──
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        miniMap = findViewById(R.id.miniMap);
        miniMap.setTileSource(TileSourceFactory.MAPNIK); //סוג מפה
        miniMap.setMultiTouchControls(false); // לא גוללים במפה הקטנה
        miniMap.getController().setZoom(7.5); // זום
        miniMap.getController().setCenter(new GeoPoint(31.5, 34.8)); // מרכז ישראל

        AddAllMarkers();

        // לחיצה על המפה -> פתיחת מסך מלא
        findViewById(R.id.mapClickOverlay).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FullMapActivity.class);
            startActivity(intent);
        });

        // ── כפתורים ──
        Button buttoToAddDonation = findViewById(R.id.btn_addDonation);
        Button buttonToSearchDonation = findViewById(R.id.btn_searchDonation);

        buttoToAddDonation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PickCatergoryActivity.class);
            startActivity(intent);
        });

        buttonToSearchDonation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchDonationsActivity.class);
            startActivity(intent);
        });
    }

    public void AddAllMarkers(){

        miniMap.getOverlays().clear();

        DatabaseService.getInstance().getDonationService()
                .getDonationsCountByCities(DonationStatus.APPROVED_AVAILABLE,
                        new DatabaseService.DatabaseCallback<java.util.HashMap<String, Integer>>() {

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
        Marker marker = new Marker(miniMap); //יצירת מרקר
        marker.setPosition(new GeoPoint(lat, lon)); // מיקום
        marker.setTitle(city + ": " + count + " תרומות"); // כיתוב
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // מרכוז וקביעה שהלמטה יגע במפה
        miniMap.getOverlays().add(marker); // הוספה למפה עצמה (לפי שכבות)
    }

    @Override
    public void onResume() {
        super.onResume();
        if (miniMap != null) miniMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (miniMap != null) miniMap.onPause();
    }

    private void updateUserLevelUI(User user) {
        int donations = user.getDonationCounter();
        UserLevel currentLevel = UserLevel.fromDonationCount(donations);

        int min = currentLevel.getMinDonations();
        int max = currentLevel.getMaxDonations() + 1;

        int progress;
        if (max == Integer.MAX_VALUE) {
            progress = 100;
        } else {
            progress = (donations - min) * 100 / (max - min);
        }

        rateProgressBar.setProgress(progress);
        currentRateIcon.setImageResource(currentLevel.getIconRes());

        UserLevel[] levels = UserLevel.values();
        int nextIndex = currentLevel.ordinal() + 1;

        if (nextIndex < levels.length) {
            nextRateIcon.setVisibility(View.VISIBLE);
            nextRateIcon.setImageResource(levels[nextIndex].getIconRes());
        } else {
            nextRateIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}