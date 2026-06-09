package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.adapters.DonationAdapter;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationCategory;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Public donation search screen — shows only approved donations from active donors
// Supports filtering by name, category, and city. Can be launched with a pre-set city from the map
public class SearchDonationsActivity extends BaseActivity {

    private static final String TAG = "SearchDonationsActivity";

    private RecyclerView recyclerView;
    private DonationAdapter donationAdapter;
    private TextView tvDonationCount;
    private MaterialButton btnToggle;
    private MaterialCardView layoutFilters;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private String searchQuery = "";
    private DonationCategory categoryFilter = null;
    private String cityFilter = null;
    private boolean filtersVisible = false;

    // kept as a field so it can be pre-populated when launched from the map
    private AutoCompleteTextView spinnerCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_donations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDonationCount = findViewById(R.id.tv_donation_count);
        layoutEmpty     = findViewById(R.id.layout_empty);
        progressBar     = findViewById(R.id.progress_bar);
        btnToggle       = findViewById(R.id.btn_toggle_filters);
        layoutFilters   = findViewById(R.id.layout_filters);
        spinnerCity     = findViewById(R.id.spinner_city);
        recyclerView    = findViewById(R.id.rv_donations_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        donationAdapter = new DonationAdapter(donation -> {
            Intent intent = new Intent(this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(donationAdapter);

        // updates the count label and toggles empty/list state after every filter
        donationAdapter.setOnFilterListener(count -> {
            tvDonationCount.setText("סה״כ: " + count);
            if (count == 0) showEmpty();
            else showList();
        });

        setupToggleFilters();
        setupSearchFilter();
        setupCategoryFilter();
        setupCityFilter();
        setupClearFilters();

        // if launched from the map with a city pre-selected, apply it immediately
        String cityFromMap = getIntent().getStringExtra("CITY_FILTER");
        if (cityFromMap != null) {
            cityFilter = cityFromMap;
            spinnerCity.setText(cityFromMap, false);
            filtersVisible = true;
            layoutFilters.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonations();
    }

    // loads all users to build an active-status map, then fetches all donations
    // and keeps only approved ones from active donors
    private void loadDonations() {
        showLoading();

        databaseService.getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        Map<String, Boolean> activeMap = new HashMap<>();
                        for (User u : users) {
                            activeMap.put(u.getId(), u.isActive());
                        }

                        databaseService.getDonationService()
                                .getAll(new DatabaseService.DatabaseCallback<List<Donation>>() {
                                    @Override
                                    public void onCompleted(List<Donation> donations) {
                                        // filters to only APPROVED_AVAILABLE donations from active donors
                                        List<Donation> available = new ArrayList<>();
                                        for (Donation d : donations) {
                                            boolean isApproved = d.getStatus() == DonationStatus.APPROVED_AVAILABLE;
                                            Boolean donorActive = activeMap.get(d.getGiverID());
                                            boolean isDonorActive = donorActive == null || donorActive;
                                            if (isApproved && isDonorActive) {
                                                available.add(d);
                                            }
                                        }
                                        runOnUiThread(() -> {
                                            donationAdapter.setDonationList(available);
                                            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
                                        });
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        Log.e(TAG, "Failed to get donations", e);
                                        runOnUiThread(() -> showEmpty());
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get users", e);
                        runOnUiThread(() -> showEmpty());
                    }
                });
    }

    // toggles the filter panel visibility on button click
    private void setupToggleFilters() {
        btnToggle.setOnClickListener(v -> {
            filtersVisible = !filtersVisible;
            layoutFilters.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
        });
    }

    // filters donations by name as the user types
    private void setupSearchFilter() {
        EditText etSearch = findViewById(R.id.et_search_donation);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
            }
        });
    }

    // dropdown for filtering by donation category
    private void setupCategoryFilter() {
        AutoCompleteTextView spinnerCategory = findViewById(R.id.spinner_category);
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("כל הקטגוריות");
        for (DonationCategory cat : DonationCategory.values()) {
            categoryNames.add(cat.getHebrewName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setText("כל הקטגוריות", false);
        // position 0 means "all categories"
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            categoryFilter = position == 0 ? null : DonationCategory.values()[position - 1];
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }

    // dropdown for filtering by city
    private void setupCityFilter() {
        String[] cities = IsraelCity.getHebrewNames();
        List<String> cityList = new ArrayList<>();
        cityList.add("כל הערים");
        cityList.addAll(Arrays.asList(cities));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cityList);
        spinnerCity.setAdapter(adapter);
        spinnerCity.setText("כל הערים", false);
        spinnerCity.setOnItemClickListener((parent, view, position, id) -> {
            cityFilter = position == 0 ? null : cities[position - 1];
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }

    // resets all active filters and refreshes the list
    private void setupClearFilters() {
        findViewById(R.id.btn_clear_filters).setOnClickListener(v -> {
            ((EditText) findViewById(R.id.et_search_donation)).setText("");
            ((AutoCompleteTextView) findViewById(R.id.spinner_category)).setText("כל הקטגוריות", false);
            spinnerCity.setText("כל הערים", false);
            searchQuery = "";
            categoryFilter = null;
            cityFilter = null;
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showList() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }
}