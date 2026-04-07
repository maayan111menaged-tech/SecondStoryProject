package com.example.secondstoryproject.services.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.IsraelCity;
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
    public void update(@NonNull String donationId,
                       @NonNull java.util.function.UnaryOperator<Donation> function,
                       @Nullable DatabaseCallback<Donation> callback) {

        super.update(donationId, function, callback);
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

    @Override
    public void getDonationsCountByStatus(@NonNull DonationStatus status,
                                          @NonNull DatabaseCallback<Integer> callback) {

        super.getAll(new DatabaseCallback<List<Donation>>() {
            @Override
            public void onCompleted(List<Donation> donations) {

                int count = 0;

                for (Donation donation : donations) {
                    if (donation.getStatus() != null &&
                            donation.getStatus() == status) {

                        count++;
                    }
                }

                callback.onCompleted(count);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void getDonationsByStatus(@NonNull DonationStatus status,
                                     @NonNull DatabaseCallback<List<Donation>> callback) {

        super.getAll(new DatabaseCallback<List<Donation>>() {
            @Override
            public void onCompleted(List<Donation> donations) {

                List<Donation> filtered = new ArrayList<>();

                for (Donation donation : donations) {
                    if (donation.getStatus() != null &&
                            donation.getStatus() == status) {

                        filtered.add(donation);
                    }
                }

                callback.onCompleted(filtered);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void getDonationsCountByCity(@NonNull IsraelCity city,
                                          @NonNull DatabaseCallback<Integer> callback) {

        super.getAll(new DatabaseCallback<List<Donation>>() {
            @Override
            public void onCompleted(List<Donation> donations) {

                int count = 0;

                for (Donation donation : donations) {
                    if (donation.getCity() != null &&
                            donation.getCity().equals(city.getHebrewName())) {

                        count++;
                    }
                }

                callback.onCompleted(count);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void getDonationsCountByCities(@NonNull DatabaseCallback<java.util.HashMap<String, Integer>> callback) {

        super.getAll(new DatabaseCallback<List<Donation>>() {
            @Override
            public void onCompleted(List<Donation> donations) {

                java.util.HashMap<String, Integer> cityCountMap = new java.util.HashMap<>();

                for (Donation donation : donations) {

                    if (donation.getCity() == null) continue;

                    String city = donation.getCity();

                    int current = cityCountMap.containsKey(city) ? cityCountMap.get(city) : 0;
                    cityCountMap.put(city, current + 1);
                }

                callback.onCompleted(cityCountMap);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }
}