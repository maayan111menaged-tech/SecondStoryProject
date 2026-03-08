package com.example.secondstoryproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;
import com.example.secondstoryproject.services.IDonationService;
import java.util.ArrayList;
import java.util.List;


/// implementation of {@link IDonationService} backed by Firebase Realtime Database
/// @see BaseFirebaseService
/// @see Donation
public class DonationServiceImpl extends BaseFirebaseService<Donation> implements IDonationService {

    public DonationServiceImpl() {
        super("donations", Donation.class);
    }

    @Override
    public String generateId() {
        return super.generateId();
    }

    @Override
    public void create(@NonNull Donation donation, @Nullable DatabaseCallback<Void> callback) {
        super.create(donation, callback);
    }

    @Override
    public void get(@NonNull String donationId, @NonNull DatabaseCallback<Donation> callback) {
        super.get(donationId, callback);
    }

    @Override
    public void getAll(@NonNull DatabaseCallback<List<Donation>> callback) {
        super.getAll(callback);
    }

    @Override
    public void delete(@NonNull String donationId, @Nullable DatabaseCallback<Void> callback) {
        super.delete(donationId, callback);
    }

    @Override
    public void getByGiverId(@NonNull String giverId, @NonNull DatabaseCallback<List<Donation>> callback) {

        super.getAll(new DatabaseCallback<List<Donation>>() {

            @Override
            public void onCompleted(List<Donation> donations) {

                List<Donation> userDonations = new ArrayList<>();

                for (Donation donation : donations) {
                    if (donation.getGiverID() != null &&
                            donation.getGiverID().equals(giverId)) {

                        userDonations.add(donation);
                    }
                }

                callback.onCompleted(userDonations);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }
}