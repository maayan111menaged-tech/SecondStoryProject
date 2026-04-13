package com.example.secondstoryproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.DonationStatus;
import com.example.secondstoryproject.models.IsraelCity;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;

/**
 * Service interface for donation-related database operations.
 * Handles all operations related to {@link Donation} entities, including:
 * - CRUD operations
 * - Filtering (by user, status, city)
 * - Statistics (counts by status and city)
 * Donations represent items shared by users in the application.
 * All operations are asynchronous and return results via {@link DatabaseCallback}
 * @see Donation
 * @see DatabaseCallback
 * @see IDatabaseService#getDonationService()
 */
public interface IDonationService {

    /**
     * Generates a unique ID for a new donation.
     * @return generated donation ID
     */
    String generateId();

    /**
     * Creates a new donation in the database.
     * The donation's {@link Donation#getId()} is used as the database key.
     * @param donation donation to create
     * @param callback optional callback for result
     */
    void create(@NonNull Donation donation, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a donation by ID.
     * @param donationId donation ID
     * @param callback result callback (may return null if not found)
     */
    void get(@NonNull String donationId, @NonNull DatabaseCallback<Donation> callback);

    /**
     * Retrieves all donations.
     * @param callback list of donations (never null, may be empty)
     */
    void getAll(@NonNull DatabaseCallback<List<Donation>> callback);

    /**
     * Deletes a donation by ID.
     * @param donationId donation ID
     * @param callback optional callback
     */
    void delete(@NonNull String donationId, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves all donations created by a specific user.
     * @param giverId user ID (donor)
     * @param callback list of donations belonging to the user
     */
    void getByGiverId(@NonNull String giverId,
                      @NonNull DatabaseCallback<List<Donation>> callback);

    /**
     * Returns the number of donations with a specific status.
     * Used for statistics and admin dashboards.
     * @param status donation status
     * @param callback number of matching donations
     */
    void getDonationsCountByStatus(@NonNull DonationStatus status,
                                   @NonNull DatabaseCallback<Integer> callback);

    /**
     * Retrieves all donations with a specific status.
     * @param status donation status
     * @param callback list of matching donations
     */
    void getDonationsByStatus(@NonNull DonationStatus status,
                              @NonNull DatabaseCallback<List<Donation>> callback);

    /**
     * Returns the number of donations in a specific city.
     * @param city city to filter by
     * @param callback number of donations in the city
     */
    void getDonationsCountByCity(@NonNull IsraelCity city,
                           @NonNull DatabaseCallback<Integer> callback);

    /**
     * Returns the number of donations grouped by cities.
     * Used for maps and statistics (e.g. showing donation distribution).
     * @param callback map of city name → donation count
     */
     void getDonationsCountByCities(@NonNull DatabaseCallback<java.util.HashMap<String,
             Integer>> callback);

    /**
     * Updates a donation using a transaction.
     * @param donationId donation ID
     * @param function transformation function
     * @param callback result callback with updated donation
     */    void update(@NonNull String donationId,
                @NonNull java.util.function.UnaryOperator<Donation> function,
                @Nullable DatabaseCallback<Donation> callback);


}