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
import android.widget.LinearLayout;
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

    private TextView tvUsersCount;
    private TextView tvPendingDonations;
    private PieChart pieChart;
    private LinearLayout legendContainer;

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

        tvUsersCount = findViewById(R.id.tv_users_count);
        tvPendingDonations = findViewById(R.id.tv_pending_donations);
        pieChart = findViewById(R.id.pie_chart);
        legendContainer = findViewById(R.id.legendContainer);

        loadStats();

        findViewById(R.id.btn_accept_donations).setOnClickListener(v ->
                startActivity(new Intent(this, AcceptDonationActivity.class)));
        findViewById(R.id.btn_users_list).setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));
        findViewById(R.id.btn_donation_list).setOnClickListener(v ->
                startActivity(new Intent(this, DonationsListActivity.class)));
        findViewById(R.id.btn_chats).setOnClickListener(v ->
                startActivity(new Intent(this, ChatsListActivity.class)));
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
        ArrayList<DonationStatus> orderedStatuses = new ArrayList<>();

        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.PENDING_APPROVAL,   getColor(R.color.status_pending));
        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.APPROVED_AVAILABLE, getColor(R.color.status_available));
        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.MATCHED,            getColor(R.color.status_matched));
        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.REJECTED,           getColor(R.color.status_rejected));
        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.CANCELLED,          getColor(R.color.status_cancelled));
        addPieEntry(entries, colors, orderedStatuses, statusCount,
                DonationStatus.DONOR_DELETED,      getColor(R.color.status_deleted));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("תרומות");
        pieChart.setCenterTextSize(14f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setRotationEnabled(true);
        pieChart.getLegend().setEnabled(false); // מכבים את ה-legend המובנה
        pieChart.animateY(800);
        pieChart.invalidate();

        buildLegend(orderedStatuses, colors);

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
                             ArrayList<DonationStatus> statuses,
                             Map<DonationStatus, Integer> statusCount,
                             DonationStatus status,
                             int color) {
        if (statusCount.containsKey(status)) {
            entries.add(new PieEntry(statusCount.get(status), status.getHebrewName()));
            colors.add(color);
            statuses.add(status);
        }
    }

    private void buildLegend(List<DonationStatus> statuses, List<Integer> colors) {
        legendContainer.removeAllViews();

        for (int i = 0; i < statuses.size(); i++) {
            DonationStatus status = statuses.get(i);
            int color = colors.get(i);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, 8);
            row.setLayoutParams(rowParams);

            // עיגול צבע
            View colorDot = new View(this);
            int dotSize = (int) (12 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotParams.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
            colorDot.setLayoutParams(dotParams);

            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(color);
            colorDot.setBackground(circle);

            // טקסט
            TextView label = new TextView(this);
            label.setText(status.getHebrewName());
            label.setTextSize(13f);
            label.setTextColor(getColor(R.color.dark_purple));

            row.addView(colorDot);
            row.addView(label);
            legendContainer.addView(row);
        }
    }
}