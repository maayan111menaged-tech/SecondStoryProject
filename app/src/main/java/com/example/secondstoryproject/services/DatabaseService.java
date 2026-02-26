package com.example.secondstoryproject.services;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.services.impl.DonationServiceImpl;
import com.example.secondstoryproject.services.impl.UserServiceImpl;


/// Singleton implementation of {@link IDatabaseService} that serves as the
/// application's service locator for all database operations.
/// <p>
/// This class lazily initializes a single instance and wires together the
/// concrete service implementations ({@link UserServiceImpl},
/// {@link DonationServiceImpl}).
/// </p>
/// <p>
/// Usage:
/// <pre>{@code
///     IDatabaseService db = DatabaseService.getInstance();
///     db.getUserService().get(uid, callback);
/// }</pre>
/// </p>
/// @see IDatabaseService
public class DatabaseService implements IDatabaseService {

    /// the single shared instance (lazily created)
    private static DatabaseService instance;

    /// service handling user-related database operations
    private final IUserService userService;
    /// service handling Donation-related database operations
    private final IDonationService DonationService;

    /// Private constructor — initializes all entity-specific service implementations.
    /// Use {@link #getInstance()} to obtain the shared instance.
    private DatabaseService() {
        userService = new UserServiceImpl();
        DonationService = new DonationServiceImpl();

    }

    /// Returns the shared {@link IDatabaseService} instance, creating it on first call.
    /// <p>
    /// This method is thread-safe — the {@code synchronized} keyword ensures that
    /// only one thread can initialize the instance at a time.
    /// </p>
    /// @return the singleton {@link IDatabaseService} instance
    public static synchronized IDatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    /// {@inheritDoc}
    @Override
    public IUserService getUserService() {
        return userService;
    }

    /// {@inheritDoc}
    @Override
    public IDonationService getDonationService() {
        return DonationService;
    }

}