package com.example.secondstoryproject.services.impl;

import android.util.Log;

import com.example.secondstoryproject.models.Rate;
import com.example.secondstoryproject.services.IDatabaseService;
import com.example.secondstoryproject.services.IRateService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RateServiceImpl implements IRateService {

    private final DatabaseReference ratingsRef;

    public RateServiceImpl() {
        ratingsRef = FirebaseDatabase.getInstance(
                        "https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("ratings");
    }

    @Override
    public void saveRate(Rate rate, IDatabaseService.DatabaseCallback<Void> callback) {
        String rateId = ratingsRef.push().getKey();
        ratingsRef.child(rateId).setValue(rate)
                .addOnSuccessListener(unused -> callback.onCompleted(null))
                .addOnFailureListener(callback::onFailed);
    }

    @Override
    public void getRatesForUser(String userId,
                                IDatabaseService.DatabaseCallback<List<Rate>> callback) {
        ratingsRef.orderByChild("ratedUserId").equalTo(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailed(task.getException());
                        return;
                    }
                    List<Rate> rates = new ArrayList<>();
                    for (DataSnapshot snap : task.getResult().getChildren()) {
                        Rate rate = snap.getValue(Rate.class);
                        if (rate != null) rates.add(rate);
                    }
                    callback.onCompleted(rates);
                });
    }

    @Override
    public void hasRated(String ratingUserId, String donationId,
                         IDatabaseService.DatabaseCallback<Boolean> callback) {

        Log.d("RATE_DEBUG", "hasRated called — ratingUserId: " + ratingUserId + ", donationId: " + donationId);

        ratingsRef.orderByChild("ratingUserId").equalTo(ratingUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("RATE_DEBUG", "Query failed: " + task.getException());
                        callback.onFailed(task.getException());
                        return;
                    }

                    Log.d("RATE_DEBUG", "Results count: " + task.getResult().getChildrenCount());

                    for (DataSnapshot snap : task.getResult().getChildren()) {
                        Rate rate = snap.getValue(Rate.class);
                        Log.d("RATE_DEBUG", "Found rate — donationId in DB: " +
                                (rate != null ? rate.getDonationId() : "null") +
                                ", looking for: " + donationId);

                        if (rate != null && donationId.equals(rate.getDonationId())) {
                            Log.d("RATE_DEBUG", "Match found — user already rated");
                            callback.onCompleted(true);
                            return;
                        }
                    }
                    Log.d("RATE_DEBUG", "No match — user has not rated");
                    callback.onCompleted(false);
                });
    }
}
