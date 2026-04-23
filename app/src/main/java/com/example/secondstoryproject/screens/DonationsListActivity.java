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
    import com.example.secondstoryproject.services.DatabaseService;
    import com.google.android.material.button.MaterialButton;

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

        private String searchQuery = "";
        private Set<DonationStatus> statusFilter = new HashSet<>(); // ריק = הכל
        private DonationCategory categoryFilter = null;
        private String cityFilter = null;
        private boolean filtersVisible = false;

        private LinearLayout layoutEmpty;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_donations_list);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            layoutEmpty = findViewById(R.id.layout_empty);

            tvDonationCount = findViewById(R.id.tv_donation_count);

            recyclerView = findViewById(R.id.rv_donations_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            donationAdapter = new DonationAdapter(donation -> {
                Log.d(TAG, "Donation clicked: " + donation.getName());
                Intent intent = new Intent(DonationsListActivity.this, DonationDetailActivity.class);
                intent.putExtra("DONATION_ID", donation.getId());
                startActivity(intent);
            });
            recyclerView.setAdapter(donationAdapter);

            donationAdapter.setOnFilterListener(count ->
                    tvDonationCount.setText("Total: " + count));

            setupToggleFilters();
            setupSearchFilter();
            setupStatusFilter();
            setupCategoryFilter();
            setupCityFilter();
            setupClearFilters();
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
                    donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
                }
            });
        }

        private void setupStatusFilter() {
            MaterialButton btnStatus = findViewById(R.id.btn_filter_status);
            DonationStatus[] allStatuses = DonationStatus.values();

            // מחרוזות להצגה בדיאלוג
            String[] statusNames = new String[allStatuses.length];
            for (int i = 0; i < allStatuses.length; i++) {
                statusNames[i] = allStatuses[i].getHebrewName();
            }

            // מערך שעוקב אחרי מה מסומן
            boolean[] checkedItems = new boolean[allStatuses.length];

            btnStatus.setOnClickListener(v -> {
                // מסנכרן את מצב הסימון הנוכחי
                for (int i = 0; i < allStatuses.length; i++) {
                    checkedItems[i] = statusFilter.contains(allStatuses[i]);
                }

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Filter by Status")
                        .setMultiChoiceItems(statusNames, checkedItems,
                                (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                        .setPositiveButton("OK", (dialog, which) -> {
                            statusFilter.clear();
                            for (int i = 0; i < allStatuses.length; i++) {
                                if (checkedItems[i]) {
                                    statusFilter.add(allStatuses[i]);
                                }
                            }
                            // עדכון טקסט הכפתור
                            if (statusFilter.isEmpty()) {
                                btnStatus.setText("Status (All)");
                            } else {
                                btnStatus.setText("Status (" + statusFilter.size() + ")");
                            }
                            donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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
                donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
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
                donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
            });
        }

        private void setupClearFilters() {
            findViewById(R.id.btn_clear_filters).setOnClickListener(v -> {
                ((EditText) findViewById(R.id.et_search_donation)).setText("");
                ((AutoCompleteTextView) findViewById(R.id.spinner_category))
                        .setText("All Categories", false);
                ((AutoCompleteTextView) findViewById(R.id.spinner_city))
                        .setText("All Cities", false);

                searchQuery = "";
                statusFilter.clear();
                categoryFilter = null;
                cityFilter = null;

                ((MaterialButton) findViewById(R.id.btn_filter_status)).setText("Status (All)");
                donationAdapter.filterAdmin(searchQuery, statusFilter, categoryFilter, cityFilter);
            });
        }

        @Override
        protected void onResume() {
            super.onResume();
            DatabaseService.getInstance().getDonationService()
                    .getAll(new DatabaseService.DatabaseCallback<List<Donation>>() {
                        @Override
                        public void onCompleted(List<Donation> donations) {
                            // כל הסטטוסים – ללא פילטר
                            donationAdapter.setDonationList(donations);
                            tvDonationCount.setText("Total: " + donations.size());

                            if (donations.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                layoutEmpty.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "Failed to get donations", e);
                        }
                    });
        }
    }