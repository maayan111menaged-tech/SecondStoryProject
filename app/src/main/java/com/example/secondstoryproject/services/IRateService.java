package com.example.secondstoryproject.services;

import com.example.secondstoryproject.models.Rate;

import java.util.List;

public interface IRateService {
    void saveRate(Rate rate, IDatabaseService.DatabaseCallback<Void> callback);
    void getRatesForUser(String userId, IDatabaseService.DatabaseCallback<List<Rate>> callback);

    void hasRated(String ratingUserId, String donationId, IDatabaseService.DatabaseCallback<Boolean> callback);
}