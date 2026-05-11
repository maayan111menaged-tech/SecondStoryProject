package com.example.secondstoryproject.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.secondstoryproject.services.IDatabaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDonationsActivity extends BaseActivity {

    private DonationAdapter donationAdapter;
    private TextView tvTitle, tvCount;

    private List<Donation> allDonations = new ArrayList<>();

    private String searchQuery = "";
    private DonationCategory categoryFilter = null;
    private DonationStatus statusFilter = null;

    private String viewMode; // "SELF" / "ADMIN" / "OTHER"
    private String userId;

    private AutoCompleteTextView spinnerCategory, spinnerStatus;
    private List<String> cats, statusNames;
    private List<DonationStatus> allowedStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_donations);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                });

        userId   = getIntent().getStringExtra("USER_ID");
        String userName = getIntent().getStringExtra("USER_NAME");
        viewMode = getIntent().getStringExtra("VIEW_MODE");
        if (viewMode == null) viewMode = "OTHER";

        tvTitle = findViewById(R.id.tvTitle);
        tvCount = findViewById(R.id.tvDonationCount);

        // כותרת לפי מצב
        if ("SELF".equals(viewMode)) {
            tvTitle.setText("התרומות שלי");
        } else {
            tvTitle.setText(userName != null ? "תרומות של " + userName : "תרומות");
        }

        RecyclerView rv = findViewById(R.id.rvDonations);
        rv.setLayoutManager(new LinearLayoutManager(this));

        donationAdapter = new DonationAdapter(donation -> {
            Intent intent = new Intent(this, DonationDetailActivity.class);
            intent.putExtra("DONATION_ID", donation.getId());
            startActivity(intent);
        });
        rv.setAdapter(donationAdapter);

        donationAdapter.setOnFilterListener(count ->
                tvCount.setText(count + " תרומות"));

        setupFilters();
        loadDonations();
    }

    private List<DonationStatus> getAllowedStatuses() {
        List<DonationStatus> list = new ArrayList<>();
        switch (viewMode) {
            case "SELF":
            case "ADMIN":
                for (DonationStatus s : DonationStatus.values()) {
                    if (s != DonationStatus.DONOR_DELETED) list.add(s);
                }
                break;
            case "OTHER":
            default:
                list.add(DonationStatus.APPROVED_AVAILABLE);
                break;
        }
        return list;
    }

    private void setupFilters() {
        // חיפוש
        com.google.android.material.textfield.TextInputEditText etSearch =
                findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                searchQuery = s.toString();
                applyFilters();
            }
        });

        // קטגוריה
        spinnerCategory = findViewById(R.id.spinnerCategory);
        cats = new ArrayList<>();
        cats.add("כל הקטגוריות");
        for (DonationCategory c : DonationCategory.values()) cats.add(c.getHebrewName());
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, cats));
        spinnerCategory.setText("כל הקטגוריות", false);
        spinnerCategory.setOnItemClickListener((p, v, pos, id) -> {
            categoryFilter = pos == 0 ? null : DonationCategory.values()[pos - 1];
            spinnerCategory.post(() -> spinnerCategory.setText(cats.get(pos), false));
            applyFilters();
        });

        // סטטוס
        spinnerStatus = findViewById(R.id.spinnerStatus);
        allowedStatuses = getAllowedStatuses();
        statusNames = new ArrayList<>();
        statusNames.add("כל הסטטוסים");
        for (DonationStatus s : allowedStatuses) statusNames.add(s.getHebrewName());
        spinnerStatus.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statusNames));
        spinnerStatus.setText("כל הסטטוסים", false);
        spinnerStatus.setOnItemClickListener((p, v, pos, id) -> {
            statusFilter = pos == 0 ? null : allowedStatuses.get(pos - 1);
            spinnerStatus.post(() -> spinnerStatus.setText(statusNames.get(pos), false));
            applyFilters();
        });

        // ✅ OTHER — מסתירים ספינר סטטוס (יש רק סטטוס אחד ממילא)
        if ("OTHER".equals(viewMode)) {
            spinnerStatus.setVisibility(View.GONE);
            // מסתירים גם את ה-TextInputLayout שעוטף אותו
            View statusLayout = (View) spinnerStatus.getParent().getParent();
            statusLayout.setVisibility(View.GONE);
        }

        // ניקוי
        findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            etSearch.setText("");
            spinnerCategory.setText("כל הקטגוריות", false);
            if (!"OTHER".equals(viewMode)) {
                spinnerStatus.setText("כל הסטטוסים", false);
                statusFilter = null;
            }
            searchQuery = "";
            categoryFilter = null;
            applyFilters();
        });
    }

    private void loadDonations() {
        if (userId == null) return;

        databaseService.getDonationService().getByGiverId(userId,
                new IDatabaseService.DatabaseCallback<List<Donation>>() {
                    @Override
                    public void onCompleted(List<Donation> donations) {
                        List<DonationStatus> allowed = getAllowedStatuses();
                        allDonations = donations.stream()
                                .filter(d -> d.getStatus() != null
                                        && allowed.contains(d.getStatus()))
                                .collect(Collectors.toList());

                        runOnUiThread(() -> {
                            donationAdapter.setDonationList(allDonations);
                            applyFilters();
                        });
                    }
                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> Toast.makeText(UserDonationsActivity.this,
                                "שגיאה בטעינת תרומות", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void applyFilters() {
        List<Donation> result = allDonations.stream()
                .filter(d -> {
                    boolean matchSearch = searchQuery.isEmpty()
                            || (d.getName() != null
                            && d.getName().toLowerCase().contains(searchQuery.toLowerCase()));
                    boolean matchCategory = categoryFilter == null
                            || d.getCategory() == categoryFilter;
                    boolean matchStatus = statusFilter == null
                            || d.getStatus() == statusFilter;
                    return matchSearch && matchCategory && matchStatus;
                })
                .collect(Collectors.toList());

        donationAdapter.setDonationList(result);
        tvCount.setText(result.size() + " תרומות");

        View emptyView = findViewById(R.id.layoutEmpty);
        View rv = findViewById(R.id.rvDonations);
        if (result.isEmpty()) {
            rv.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rv.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}