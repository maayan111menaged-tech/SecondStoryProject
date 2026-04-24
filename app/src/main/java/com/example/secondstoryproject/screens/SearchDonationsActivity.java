package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchDonationsActivity extends BaseActivity {

    private static final String TAG = "SearchDonationsActivity";

    private DonationAdapter donationAdapter;
    private TextView tvDonationCount;

    private String searchQuery = "";
    private DonationCategory categoryFilter = null;
    private String cityFilter = null;
    private boolean filtersVisible = false;

    private AutoCompleteTextView spinnerCity;
    private MaterialButton btnToggle;
    private LinearLayout layoutFilters;

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

        RecyclerView recyclerView = findViewById(R.id.rv_donations_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        donationAdapter = new DonationAdapter(donation -> {
            Intent intent = new Intent(SearchDonationsActivity.this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(donationAdapter);

        donationAdapter.setOnFilterListener(count ->
                tvDonationCount.setText("Total: " + count));

        setupToggleFilters();
        setupSearchFilter();
        setupCategoryFilter();
        setupCityFilter();
        setupClearFilters();

        spinnerCity = findViewById(R.id.spinner_city);
        btnToggle = findViewById(R.id.btn_toggle_filters);
        layoutFilters = findViewById(R.id.layout_filters);

        String cityFromMap = getIntent().getStringExtra("CITY_FILTER");
        if (cityFromMap != null) {
            cityFilter = cityFromMap;
            spinnerCity.setText(cityFromMap, false);
            filtersVisible = true;
            layoutFilters.setVisibility(View.VISIBLE);
            btnToggle.setText("🔍 Hide Filters");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonationsWithActiveUsersOnly();
    }

    /**
     * ✅ טוען תרומות + משתמשים, ומסנן תרומות שהתורם שלהן פעיל בלבד.
     */
    private void loadDonationsWithActiveUsersOnly() {
        // קודם טוענים את כל המשתמשים לבניית מפה uid → isActive
        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        // בונים מפה מהירה: userId → isActive
                        Map<String, Boolean> activeMap = new HashMap<>();
                        for (User u : users) {
                            activeMap.put(u.getId(), u.isActive());
                        }

                        // עכשיו טוענים תרומות
                        DatabaseService.getInstance().getDonationService()
                                .getAll(new DatabaseService.DatabaseCallback<List<Donation>>() {
                                    @Override
                                    public void onCompleted(List<Donation> donations) {
                                        List<Donation> available = new ArrayList<>();
                                        for (Donation d : donations) {
                                            boolean isApproved = d.getStatus() == DonationStatus.APPROVED_AVAILABLE;
                                            // ✅ מציגים רק אם התורם פעיל (ברירת מחדל: פעיל אם לא נמצא במפה)
                                            Boolean donorActive = activeMap.get(d.getGiverID());
                                            boolean isDonorActive = donorActive == null || donorActive;
                                            if (isApproved && isDonorActive) {
                                                available.add(d);
                                            }
                                        }
                                        donationAdapter.setDonationList(available);
                                        donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
                                        tvDonationCount.setText("Total: " + available.size());
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        Log.e(TAG, "Failed to get donations", e);
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get users", e);
                    }
                });
    }

    private void setupToggleFilters() {
        MaterialButton btnToggle = findViewById(R.id.btn_toggle_filters);
        LinearLayout layoutFilters = findViewById(R.id.layout_filters);
        btnToggle.setOnClickListener(v -> {
            filtersVisible = !filtersVisible;
            layoutFilters.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
            btnToggle.setText(filtersVisible ? "🔍 Hide Filters" : "🔍 Show Filters");
        });
    }

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

    private void setupCategoryFilter() {
        AutoCompleteTextView spinnerCategory = findViewById(R.id.spinner_category);
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("All Categories");
        for (DonationCategory cat : DonationCategory.values()) {
            categoryNames.add(cat.getHebrewName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setText("All Categories", false);
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            categoryFilter = position == 0 ? null : DonationCategory.values()[position - 1];
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }

    private void setupCityFilter() {
        AutoCompleteTextView spinnerCity = findViewById(R.id.spinner_city);
        String[] cities = IsraelCity.getHebrewNames();
        List<String> cityList = new ArrayList<>();
        cityList.add("All Cities");
        cityList.addAll(Arrays.asList(cities));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cityList);
        spinnerCity.setAdapter(adapter);
        spinnerCity.setText("All Cities", false);
        spinnerCity.setOnItemClickListener((parent, view, position, id) -> {
            cityFilter = position == 0 ? null : cities[position - 1];
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }

    private void setupClearFilters() {
        findViewById(R.id.btn_clear_filters).setOnClickListener(v -> {
            ((EditText) findViewById(R.id.et_search_donation)).setText("");
            ((AutoCompleteTextView) findViewById(R.id.spinner_category)).setText("All Categories", false);
            ((AutoCompleteTextView) findViewById(R.id.spinner_city)).setText("All Cities", false);
            searchQuery = "";
            categoryFilter = null;
            cityFilter = null;
            donationAdapter.filter(searchQuery, categoryFilter, cityFilter);
        });
    }
}