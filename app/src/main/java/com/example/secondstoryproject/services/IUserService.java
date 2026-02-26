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

    /// generate a unique id for a new user
    /// @return a new id
    String generateId();

    /// create a new user in the database.
    /// the user's {@link User#getId()} is used as the database key.
    /// @param user the user to create (must have a valid id)
    /// @param callback called with {@code null} on success, or an exception on failure.
    ///                 may be null if the caller doesn't need to handle the result.
    void create(@NonNull User user, @Nullable DatabaseCallback<Void> callback);

    /// retrieve a single user by their id
    /// @param uid the unique id of the user
    /// @param callback called with the {@link User} object on success, or an exception on failure.
    ///                 the user may be {@code null} if no user exists with the given id.
    void get(@NonNull String uid, @NonNull DatabaseCallback<User> callback);

    /// retrieve all users from the database
    /// @param callback called with a {@link List} of all {@link User} objects on success,
    ///                 or an exception on failure. the list may be empty but never null.
    void getAll(@NonNull DatabaseCallback<List<User>> callback);

    /// delete a user from the database by their id
    /// @param uid the unique id of the user to delete
    /// @param callback called with {@code null} on success, or an exception on failure.
    ///                 may be null if the caller doesn't need to handle the result.
    void delete(@NonNull String uid, @Nullable DatabaseCallback<Void> callback);

    /// update a user in the database using a transaction.
    /// the {@link UnaryOperator} receives the current user state and returns the updated state.
    /// this is safe for concurrent modifications.
    /// @param userId the unique id of the user to update
    /// @param function a function that takes the current {@link User} and returns the updated {@link User}
    /// @param callback called with {@code null} on success, or an exception on failure.
    ///                 may be null if the caller doesn't need to handle the result.
    void update(@NonNull String userId, @NonNull UnaryOperator<User> function, @Nullable DatabaseCallback<User> callback);

    /// find a user matching the given username and password.
    /// used for authentication / login.
    /// @param username the username to match
    /// @param password the password to match
    /// @param callback called with the matching {@link User} on success,
    ///                 or {@code null} if no user matches the given credentials.
    void getUserByUserNameAndPassword(@NonNull String username, @NonNull String password, @NonNull DatabaseCallback<User> callback);

    /// check whether an username is already registered in the database.
    /// used during registration to prevent duplicate accounts.
    /// @param username the username address to check
    /// @param callback called with {@code true} if the username exists, {@code false} otherwise.
    void checkIfUserNameExists(@NonNull String username, @NonNull DatabaseCallback<Boolean> callback);

    /// find a user matching the given username
    /// used to retrieve a user by their username
    /// @param username the username to match
    /// @param callback called with the matching {@link User} on success,
    ///                 or {@code null} if no user matches the given username.
    void findUserByUserName(@NonNull String username, @NonNull DatabaseCallback<User> callback);

}