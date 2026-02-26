package com.example.secondstoryproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * Service interface for Donation-related database operations.
 * <p>
 * Provides CRUD operations for {@link Donation} entities.
 * Donations represent items that users can give or share via the app.
 * </p>
 * <p>
 * All operations are asynchronous and return results via {@link DatabaseCallback}.
 * </p>
 * @see Donation
 * @see DatabaseCallback
 * @see IDatabaseService#getDonationService()
 */
public interface IDonationService {

    /**
     * Generate a unique ID for a new donation.
     * @return a new unique ID string
     */
    String generateId();

    /**
     * Create a new donation in the database.
     * The donation's {@link Donation#getId()} is used as the database key.
     * @param donation the donation to create (must have a valid ID)
     * @param callback called with {@code null} on success, or an exception on failure.
     *                 Can be {@code null} if the caller does not need to handle the result.
     */
    void create(@NonNull Donation donation, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieve a single donation by its ID.
     * @param donationId the unique ID of the donation
     * @param callback called with the {@link Donation} object on success, or an exception on failure.
     *                 The donation may be {@code null} if no donation exists with the given ID.
     */
    void get(@NonNull String donationId, @NonNull DatabaseCallback<Donation> callback);

    /**
     * Retrieve all donations from the database.
     * @param callback called with a {@link List} of all {@link Donation} objects on success,
     *                 or an exception on failure. The list may be empty but never null.
     */
    void getAll(@NonNull DatabaseCallback<List<Donation>> callback);

    /**
     * Delete a donation from the database by its ID.
     * @param donationId the unique ID of the donation to delete
     * @param callback called with {@code null} on success, or an exception on failure.
     *                 Can be {@code null} if the caller does not need to handle the result.
     */
    void delete(@NonNull String donationId, @Nullable DatabaseCallback<Void> callback);
}