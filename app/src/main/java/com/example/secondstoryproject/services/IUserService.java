package com.example.secondstoryproject.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;

import java.util.List;
import java.util.function.UnaryOperator;

/// Service interface for user-related database operations.
/// <p>
/// Provides CRUD operations for {@link User} entities, as well as
/// authentication-related queries (login by email/password, email existence check).
/// </p>
/// <p>
/// All operations are asynchronous and return results via {@link DatabaseCallback}.
/// </p>
/// @see User
/// @see DatabaseCallback
/// @see IDatabaseService#getUserService()
public interface IUserService {

    /**
     * Generates a unique ID for a new user.
     * @return generated user ID
     */
    String generateId();

    /**
     * Creates a new user in the database.
     * The user's {@link User#getId()} is used as the database key.
     * @param user     user to create (must contain a valid ID)
     * @param callback optional callback for success/failure
     */
    void create(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /**
     * Retrieves a user by ID.
     * @param uid      user ID
     * @param callback result callback (may return null if not found)
     */
    void get(@NonNull String uid, @NonNull DatabaseCallback<User> callback);

    /**
     * Retrieves all users in the system.
     * @param callback list of users (never null, may be empty)
     */
    void getAll(@NonNull DatabaseCallback<List<User>> callback);

    /**
     * Deletes a user by ID.
     * @param uid      user ID
     * @param callback optional callback for result
     */
    void delete(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);

    /**
     * Updates a user using a transactional operation.
     * The function receives the current state and returns the updated state.
     * @param userId   user ID
     * @param function transformation function
     * @param callback result callback
     */
    void update(@NonNull String userId, @NonNull UnaryOperator<User> function, @Nullable DatabaseCallback<User> callback);

    /**
     * Authenticates a user by username and password.
     * Used for login.
     * @param username username
     * @param password password
     * @param callback matching user or null if invalid credentials
     */
    void getUserByUserNameAndPassword(@NonNull String username, @NonNull String password, @NonNull DatabaseCallback<User> callback);

    /**
     * Checks if a username already exists in the system.
     * Used during registration to prevent duplicates.
     * @param username username to check
     * @param callback true if exists, false otherwise
     */
    void checkIfUserNameExists(@NonNull String username, @NonNull DatabaseCallback<Boolean> callback);

    /**
     * Finds a user by username.
     * @param username username to search
     * @param callback matching user or null if not found
     */
    void findUserByUserName(@NonNull String username, @NonNull DatabaseCallback<User> callback);

    /**
     * Returns the total number of registered users.
     * Used for admin dashboard and statistics screens.
     * @param callback number of users
     */
    void getUsersCount(@NonNull DatabaseCallback<Integer> callback);

    /**
     * Checks if a user is an admin.
     * @param userId user ID
     * @param callback true if admin, false otherwise
     */
    void isAdmin(@NonNull String userId,
                 @NonNull DatabaseCallback<Boolean> callback);
}