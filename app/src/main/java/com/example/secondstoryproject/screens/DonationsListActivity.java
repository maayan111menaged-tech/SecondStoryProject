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
import com.example.secondstoryproject.services.DatabaseService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DonationsListActivity extends BaseActivity {

    private static final String TAG = "DonationsListActivity";

    private RecyclerView recyclerView;
    private DonationAdapter donationAdapter;
    private TextView tvDonationCount;
    private MaterialButton btnStatus;
    private MaterialButton btnToggle;
    private MaterialCardView layoutFilters;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private String searchQuery = "";
    private Set<DonationStatus> statusFilter = new HashSet<>();
    private DonationCategory categoryFilter = null;
    private String cityFilter = null;
    private boolean filtersVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donations_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        layoutEmpty   = findViewById(R.id.layout_empty);
        progressBar   = findViewById(R.id.progress_bar);
        btnStatus     = findViewById(R.id.btn_filter_status);
        btnToggle     = findViewById(R.id.btn_toggle_filters);
        layoutFilters = findViewById(R.id.layout_filters);
        tvDonationCount = findViewById(R.id.tv_donation_count);

        recyclerView = findViewById(R.id.rv_donations_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        donationAdapter = new DonationAdapter(donation -> {
            Intent intent = new Intent(DonationsListActivity.this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(donationAdapter);

        //  עדכון ספירה + showList/showEmpty אחרי כל פילטור
        donationAdapter.setOnFilterListener(count -> {
            tvDonationCount.setText("סה״כ: " + count);
            if (count == 0) showEmpty();
            else showList();
        });

        setupToggleFilters();
        setupSearchFilter();
        setupStatusFilter();
        setupCategoryFilter();
        setupCityFilter();
        setupClearFilters();

        // פתיחה עם סטטוס מוגדר מראש
        String statusFromMain = getIntent().getStringExtra("FILTER_STATUS");
        if (statusFromMain != null) {
            DonationStatus status = DonationStatus.fromString(statusFromMain);
            statusFilter.add(status);
            btnStatus.setText("סטטוס: " + status.getHebrewName());
            filtersVisible = true;
            layoutFilters.setVisibility(View.VISIBLE);
        }
    }

    private void setupToggleFilters() {
        btnToggle.setOnClickListener(v -> {
            filtersVisible = !filtersVisible;
            layoutFilters.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);
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
                donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
            }
        });
    }

    private void setupStatusFilter() {
        DonationStatus[] allStatuses = DonationStatus.values();
        String[] statusNames = new String[allStatuses.length];
        for (int i = 0; i < allStatuses.length; i++) {
            statusNames[i] = allStatuses[i].getHebrewName();
        }
        boolean[] checkedItems = new boolean[allStatuses.length];

        btnStatus.setOnClickListener(v -> {
            for (int i = 0; i < allStatuses.length; i++) {
                checkedItems[i] = statusFilter.contains(allStatuses[i]);
            }
            new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle("סינון לפי סטטוס")
                    .setMultiChoiceItems(statusNames, checkedItems,
                            (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                    .setPositiveButton("אישור", (dialog, which) -> {
                        statusFilter.clear();
                        for (int i = 0; i < allStatuses.length; i++) {
                            if (checkedItems[i]) statusFilter.add(allStatuses[i]);
                        }
                        btnStatus.setText(statusFilter.isEmpty()
                                ? "סטטוס: הכל"
                                : "סטטוס (" + statusFilter.size() + ")");
                        donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
                    })
                    .setNegativeButton("ביטול", null)
                    .show();
        });
    }

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
        spinnerCategory.setText(categoryNames.get(0), false);

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            categoryFilter = position == 0 ? null : DonationCategory.values()[position - 1];
            donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
        });

    }

    private void setupCityFilter() {
        AutoCompleteTextView spinnerCity = findViewById(R.id.spinner_city);

        String[] cities = IsraelCity.getHebrewNames();
        List<String> cityList = new ArrayList<>();
        cityList.add("כל הערים");
        cityList.addAll(Arrays.asList(cities));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cityList);
        spinnerCity.setAdapter(adapter);
        spinnerCity.setText(cityList.get(0), false);

        spinnerCity.setOnItemClickListener((parent, view, position, id) -> {
            cityFilter = position == 0 ? null : cities[position - 1];
            donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
        });
    }

    private void setupClearFilters() {
        findViewById(R.id.btn_clear_filters).setOnClickListener(v -> {
            ((EditText) findViewById(R.id.et_search_donation)).setText("");
            ((AutoCompleteTextView) findViewById(R.id.spinner_category)).setText("כל הקטגוריות", false);
            ((AutoCompleteTextView) findViewById(R.id.spinner_city)).setText("כל הערים", false);

            searchQuery = "";
            statusFilter.clear();
            categoryFilter = null;
            cityFilter = null;

            btnStatus.setText("סטטוס: הכל");
            donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading();
        DatabaseService.getInstance().getDonationService()
                .getAll(new DatabaseService.DatabaseCallback<List<Donation>>() {
                    @Override
                    public void onCompleted(List<Donation> donations) {
                        runOnUiThread(() -> {
                            donationAdapter.setDonationList(donations);
                            // ה-OnFilterListener יטפל ב-showList/showEmpty
                            donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
                        });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Log.e(TAG, "Failed to get donations", e);
                        runOnUiThread(() -> showEmpty());
                    }
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