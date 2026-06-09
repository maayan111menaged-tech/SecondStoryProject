package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.CarouselDonationAdapter;
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

import java.util.ArrayList;
import java.util.List;

// Main screen for regular users — shows greeting, user level progress, donation carousel and a mini map.
public class MainActivity extends BaseActivity {

    // marks the home icon as selected in the bottom navigation bar
    @Override
    protected int getSelectedBottomNavItem() { return R.id.menu_home; }

    private ProgressBar rateProgressBar;
    private ImageView currentRateIcon, maxLevelIcon;
    private MapView miniMap;
    private TextView progressText, remainingText, totalDonationsText;
    private TextView currentLevelName, nextLevelName, maxLevelTitle, maxLevelSub;
    private LinearLayout normalLevelLayout, maxLevelLayout;
    private RecyclerView rvCarousel;
    private CarouselDonationAdapter carouselAdapter;
    private final List<Donation> carouselDonations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connects all XML views to their Java variables
        bindViews();

        User currentUser = SharedPreferencesUtil.getUser(this);

        // displays a greeting with the user's full name
        TextView tvGreeting = findViewById(R.id.tvGreetingName);
        tvGreeting.setText(currentUser.getFullName() + " 👋");

        // updates the level progress bar and level icons based on donation count
        updateUserLevelUI(currentUser);

        // sets up the horizontal donations carousel
        setupCarousel();
        loadCarouselDonations();

        // sets up the mini map showing donations by city
        setupMap();

        // navigates to the add donation flow
        findViewById(R.id.btn_addDonation).setOnClickListener(v ->
                startActivity(new Intent(this, PickCatergoryActivity.class)));

        // navigates to the donation search screen
        findViewById(R.id.btn_searchDonation).setOnClickListener(v ->
                startActivity(new Intent(this, SearchDonationsActivity.class)));

        // tapping the mini map opens the full screen map
        findViewById(R.id.mapClickOverlay).setOnClickListener(v ->
                startActivity(new Intent(this, FullMapActivity.class)));
    }

    // finds and stores references to all views in the layout
    private void bindViews() {
        rateProgressBar   = findViewById(R.id.rateProgressBar);
        currentRateIcon   = findViewById(R.id.currentRateIcon);
        progressText      = findViewById(R.id.progressText);
        remainingText     = findViewById(R.id.remainingText);
        totalDonationsText= findViewById(R.id.totalDonationsText);
        currentLevelName  = findViewById(R.id.currentLevelName);
        nextLevelName     = findViewById(R.id.nextLevelName);
        maxLevelLayout    = findViewById(R.id.maxLevelLayout);
        normalLevelLayout = findViewById(R.id.normalLevelLayout);
        maxLevelIcon      = findViewById(R.id.maxLevelIcon);
        maxLevelTitle     = findViewById(R.id.maxLevelTitle);
        maxLevelSub       = findViewById(R.id.maxLevelSub);
        rvCarousel        = findViewById(R.id.rvDonationsCarousel);
    }

    // initializes the carousel RecyclerView with a horizontal layout and shows a loading spinner
    private void setupCarousel() {
        carouselAdapter = new CarouselDonationAdapter(this, carouselDonations);
        rvCarousel.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCarousel.setAdapter(carouselAdapter);

        // shows loading state until donations are fetched
        rvCarousel.setVisibility(View.GONE);
        findViewById(R.id.layout_empty).setVisibility(View.GONE);
        findViewById(R.id.carouselLoading).setVisibility(View.VISIBLE);
    }

    // fetches approved donations from the DB and filters out the current user's own donations
    private void loadCarouselDonations() {
        User currentUser = SharedPreferencesUtil.getUser(this);

        DatabaseService.getInstance().getDonationService()
                .getDonationsByStatus(DonationStatus.APPROVED_AVAILABLE,
                        new DatabaseService.DatabaseCallback<List<Donation>>() {
                            @Override
                            public void onCompleted(List<Donation> donations) {
                                List<Donation> others = new ArrayList<>();
                                for (Donation d : donations) {
                                    // excludes donations that belong to the current user
                                    if (!d.getGiverID().equals(currentUser.getId())) {
                                        others.add(d);
                                    }
                                }
                                runOnUiThread(() -> {
                                    carouselDonations.clear();
                                    carouselDonations.addAll(others);
                                    // notifies the adapter that the data changed so it redraws the list
                                    carouselAdapter.notifyDataSetChanged();
                                    updateCarouselState(others.isEmpty());
                                });
                            }
                            @Override
                            public void onFailed(Exception e) {
                                runOnUiThread(() -> updateCarouselState(false));
                            }
                        });
    }

    // shows the carousel or the empty state depending on whether there are donations to display
    private void updateCarouselState(boolean isEmpty) {
        rvCarousel.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        findViewById(R.id.layout_empty).setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        findViewById(R.id.carouselLoading).setVisibility(View.GONE);
    }

    // initializes the mini map centered on Israel and loads city markers
    private void setupMap() {
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        miniMap = findViewById(R.id.miniMap);
        miniMap.setTileSource(TileSourceFactory.MAPNIK);
        // disables touch controls — the user can only tap to open the full map
        miniMap.setMultiTouchControls(false);
        miniMap.getController().setZoom(7.5);
        miniMap.getController().setCenter(new GeoPoint(31.5, 34.8));
        AddAllMarkers();
    }

    // fetches donation counts per city from the DB and places a marker for each city
    public void AddAllMarkers() {
        miniMap.getOverlays().clear();
        databaseService.getDonationService()
                .getDonationsCountByCities(DonationStatus.APPROVED_AVAILABLE,
                        new DatabaseService.DatabaseCallback<java.util.HashMap<String, Integer>>() {
                            @Override
                            public void onCompleted(java.util.HashMap<String, Integer> cityCountMap) {
                                for (IsraelCity city : IsraelCity.values()) {
                                    // defaults to 0 if the city has no donations
                                    int count = cityCountMap.containsKey(city.getHebrewName())
                                            ? cityCountMap.get(city.getHebrewName()) : 0;
                                    addCityMarker(city.getHebrewName(),
                                            city.getLatitude(), city.getLongitude(), count);
                                }
                            }
                            @Override
                            public void onFailed(Exception e) {}
                        });
    }

    // places a single marker on the map for the given city with its donation count as the title
    private void addCityMarker(String city, double lat, double lon, int count) {
        Marker marker = new Marker(miniMap);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle(city + ": " + count + " תרומות");
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        miniMap.getOverlays().add(marker);
    }

    // updates the level section — shows max level layout if the user reached the top, otherwise shows progress
    private void updateUserLevelUI(User user) {
        int donations = user.getDonationCounter();
        UserLevel currentLevel = UserLevel.fromDonationCount(donations);
        UserLevel[] levels = UserLevel.values();
        int nextIndex = currentLevel.ordinal() + 1;
        boolean isMaxLevel = nextIndex >= levels.length;

        if (isMaxLevel) {
            normalLevelLayout.setVisibility(View.GONE);
            maxLevelLayout.setVisibility(View.VISIBLE);
            maxLevelIcon.setImageResource(currentLevel.getIconRes());
            maxLevelTitle.setText(currentLevel.getLabel());
            maxLevelSub.setText("הגעת לרמה הגבוהה ביותר עם " + donations + " תרומות!");
        } else {
            normalLevelLayout.setVisibility(View.VISIBLE);
            maxLevelLayout.setVisibility(View.GONE);
            UserLevel nextLevel = levels[nextIndex];
            int min = currentLevel.getMinDonations();
            int nextMin = nextLevel.getMinDonations();
            int doneInLevel = donations - min;
            int totalInLevel = nextMin - min;
            int remaining = nextMin - donations;
            // calculates progress percentage within the current level
            int progress = doneInLevel * 100 / totalInLevel;
            rateProgressBar.setProgress(progress);
            currentRateIcon.setImageResource(currentLevel.getIconRes());
            currentLevelName.setText(currentLevel.getLabel());
            nextLevelName.setText(nextLevel.getLabel());
            progressText.setText(doneInLevel + " מתוך " + totalInLevel);
            remainingText.setText("עוד " + remaining + " לרמה הבאה");
            totalDonationsText.setText("סה\"כ תרומות: " + donations);
        }
    }

    // required by the map library — must be called to resume map rendering when returning to the screen
    @Override
    public void onResume() {
        super.onResume();
        if (miniMap != null) miniMap.onResume();
    }

    // required by the map library — must be called to pause map rendering when leaving the screen
    @Override
    public void onPause() {
        super.onPause();
        if (miniMap != null) miniMap.onPause();
    }

}