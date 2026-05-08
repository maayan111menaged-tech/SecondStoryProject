package com.example.secondstoryproject.screens;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.DatabaseService;
import com.example.secondstoryproject.services.IDatabaseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.ArrayList;

public class AdminMainActivity extends BaseActivity {

    private Button btnAcceptDonations;
    private Button btnUsersList;
    private Button btnDonationList;
    private TextView tvUsersCount;
    private TextView tvPendingDonations;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav.setSelectedItemId(R.id.menu_home);

        btnAcceptDonations = findViewById(R.id.btn_accept_donations);
        btnUsersList = findViewById(R.id.btn_users_list);
        btnDonationList = findViewById(R.id.btn_donation_list);
        tvUsersCount = findViewById(R.id.tv_users_count);
        tvPendingDonations = findViewById(R.id.tv_pending_donations);
        pieChart = findViewById(R.id.pie_chart);

        loadStats();

        btnAcceptDonations.setOnClickListener(v ->
                startActivity(new Intent(this, AcceptDonationActivity.class)));
        btnUsersList.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));
        btnDonationList.setOnClickListener(v ->
                startActivity(new Intent(this, DonationsListActivity.class)));
    }

    private void loadStats() {
        tvPendingDonations.setText("...");
        tvUsersCount.setText("...");

        DatabaseService.getInstance().getUserService().getAll(
                new DatabaseService.DatabaseCallback<List<User>>() {
                    @Override
                    public void onCompleted(List<User> users) {
                        int activeCount = 0;
                        Map<String, Boolean> activeMap = new HashMap<>();
                        for (User u : users) {
                            activeMap.put(u.getId(), u.isActive());
                            if (u.isActive()) activeCount++;
                        }
                        final int finalActiveCount = activeCount;

                        // טוענים את כל התרומות
                        DatabaseService.getInstance().getDonationService().getAll(
                                new DatabaseService.DatabaseCallback<List<Donation>>() {
                                    @Override
                                    public void onCompleted(List<Donation> donations) {
                                        int pendingCount = 0;
                                        List<Donation> activeDonations = new ArrayList<>();

                                        for (Donation d : donations) {
                                            Boolean donorActive = activeMap.get(d.getGiverID());
                                            if (donorActive == null || donorActive) {
                                                activeDonations.add(d);
                                                if (d.getStatus() == DonationStatus.PENDING_APPROVAL) {
                                                    pendingCount++;
                                                }
                                            }
                                        }

                                        final int finalPending = pendingCount;
                                        final List<Donation> finalDonations = activeDonations;

                                        runOnUiThread(() -> {
                                            tvUsersCount.setText(String.valueOf(finalActiveCount));
                                            tvPendingDonations.setText(String.valueOf(finalPending));
                                            setupPieChart(finalDonations);
                                        });
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        runOnUiThread(() -> tvPendingDonations.setText("0"));
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Exception e) {
                        runOnUiThread(() -> {
                            tvUsersCount.setText("0");
                            tvPendingDonations.setText("0");
                        });
                    }
                });
    }
    private void setupPieChart(List<Donation> allDonations) {
        Map<DonationStatus, Integer> statusCount = new HashMap<>();
        for (Donation d : allDonations) {
            DonationStatus s = d.getStatus();
            if (s != null) {
                statusCount.put(s, statusCount.getOrDefault(s, 0) + 1);
            }
        }

        if (statusCount.isEmpty()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // סדר קבוע וצבע לכל סטטוס
        addPieEntry(entries, colors, statusCount, DonationStatus.PENDING_APPROVAL,   "#FFC107");
        addPieEntry(entries, colors, statusCount, DonationStatus.APPROVED_AVAILABLE, "#4CAF50");
        addPieEntry(entries, colors, statusCount, DonationStatus.MATCHED,            "#2196F3");
        addPieEntry(entries, colors, statusCount, DonationStatus.REJECTED,           "#F44336");
        addPieEntry(entries, colors, statusCount, DonationStatus.CANCELLED,          "#9E9E9E");
        addPieEntry(entries, colors, statusCount, DonationStatus.DONOR_DELETED,      "#795548");

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("תרומות");
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setRotationEnabled(true);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.animateY(800);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (!(e instanceof PieEntry)) return;
                String label = ((PieEntry) e).getLabel();

                DonationStatus selectedStatus = null;
                for (DonationStatus s : DonationStatus.values()) {
                    if (s.getHebrewName().equals(label)) {
                        selectedStatus = s;
                        break;
                    }
                }

                if (selectedStatus == null) return;

                Intent intent = new Intent(AdminMainActivity.this, DonationsListActivity.class);
                intent.putExtra("FILTER_STATUS", selectedStatus.name());
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {}
        });

    }

    private void addPieEntry(ArrayList<PieEntry> entries,
                             ArrayList<Integer> colors,
                             Map<DonationStatus, Integer> statusCount,
                             DonationStatus status,
                             String hex) {
        if (statusCount.containsKey(status)) {
            entries.add(new PieEntry(statusCount.get(status), status.getHebrewName()));
            colors.add(Color.parseColor(hex));
        }
    }
}