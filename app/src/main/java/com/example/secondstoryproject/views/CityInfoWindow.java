package com.example.secondstoryproject.views;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.screens.SearchDonationsActivity;

public class CityInfoWindow extends InfoWindow {

    private final String cityName;
    private final int donationCount;

    public CityInfoWindow(MapView mapView, String cityName, int donationCount) {
        super(R.layout.marker_info_window, mapView);
        this.cityName = cityName;
        this.donationCount = donationCount;
    }

    @Override
    public void onOpen(Object item) {
        TextView tvCity = mView.findViewById(R.id.tv_city_name);
        TextView tvCount = mView.findViewById(R.id.tv_donation_count);
        Button btnSearch = mView.findViewById(R.id.btn_search);

        tvCity.setText(cityName);
        tvCount.setText(donationCount + " תרומות זמינות");

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(mView.getContext(), SearchDonationsActivity.class);
            intent.putExtra("CITY_FILTER", cityName);
            mView.getContext().startActivity(intent);
            close();
        });

    }

    @Override
    public void onClose() {}
}