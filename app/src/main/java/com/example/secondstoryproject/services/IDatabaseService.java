package com.example.secondstoryproject.services;

/// Service locator interface for accessing entity-specific database services.
/// <p>
/// This is the main entry point for all database operations in the application.
/// Rather than exposing database methods directly, it provides access to
/// segregated service interfaces — each responsible for a single entity type.
/// </p>
/// <p>
/// Usage example:
/// <pre>{@code
///     IDatabaseService db = DatabaseService.getInstance();
///     db.getUserService().get(uid, callback);
///     db.getFoodService().getAll(callback);
///     db.getCartService().create(cart, callback);
/// }</pre>
/// </p>
/// @see IUserService
/// @see IDonationService
/// @see DatabaseService
public interface IDatabaseService {

    /// get the user service for performing user-related database operations
    /// @return an implementation of {@link IUserService}
    IUserService getUserService();

    /// get the food service for performing food-related database operations
    /// @return an implementation of {@link IDonationService}
    IDonationService getDonationService();


    /// Generic callback interface for asynchronous database operations.
    /// <p>
    /// All database operations are asynchronous. Use this callback to handle
    /// the result of an operation when it completes.
    /// </p>
    /// <p>
    /// Exactly one of {@link #onCompleted(Object)} or {@link #onFailed(Exception)}
    /// will be called for each operation — never both.
    /// </p>
    /// @param <T> the type of the result returned on success
    interface DatabaseCallback<T> {
        /// called when the operation completes successfully
        /// @param object the result of the operation, or null for void operations
        void onCompleted(T object);

        /// called when the operation fails
        /// @param e the exception describing what went wrong
        void onFailed(Exception e);
    }
}
