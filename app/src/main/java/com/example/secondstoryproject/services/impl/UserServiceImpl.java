package com.example.secondstoryproject.services.impl;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;
import com.example.secondstoryproject.services.IUserService;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Firebase-based implementation of {@link IUserService}.
 * Handles all user-related database operations including:
 * - CRUD operations (via {@link BaseFirebaseService})
 * - Authentication (login)
 * - Username validation and lookup
 * - User statistics
 * This class extends {@link BaseFirebaseService} to reuse generic
 * Firebase CRUD functionality.
 */
public class UserServiceImpl extends BaseFirebaseService<User> implements IUserService {

    /**
     * Constructor.
     * Initializes the service with the "users" database path.
     */
    public UserServiceImpl() {
        super("users", User.class);
    }

    @Override
    public String generateId() {
        return super.generateId();
    }

    @Override
    public void create(@NonNull User user, @Nullable DatabaseCallback<Void> callback) {
        super.create(user, callback);
    }

    @Override
    public void get(@NonNull String uid, @NonNull DatabaseCallback<User> callback) {
        super.get(uid, callback);
    }

    @Override
    public void getAll(@NonNull DatabaseCallback<List<User>> callback) {
        super.getAll(callback);
    }

    @Override
    public void delete(@NonNull String uid, @Nullable DatabaseCallback<Void> callback) {
        super.delete(uid, callback);
    }

    @Override
    public void update(@NonNull String userId, @NonNull UnaryOperator<User> function, @Nullable DatabaseCallback<User> callback) {
        super.update(userId, function, callback);
    }

    /**
     * Finds a user by username and password.
     * Used for authentication (login).
     * @param username username
     * @param password password
     * @param callback matching user or null if credentials are invalid
     */
    @Override
    public void getUserByUserNameAndPassword(@NonNull final String username, @NonNull final String password, @NonNull final DatabaseCallback<User> callback) {
        getAll(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getUserName(), username) && Objects.equals(user.getPassword(), password)) {
                        callback.onCompleted(user);
                        return;
                    }
                }
                callback.onCompleted(null);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Checks if a username already exists in the system.
     * Used during user registration.
     * @param username username to check
     * @param callback true if exists, false otherwise
     */
    @Override
    public void checkIfUserNameExists(@NonNull final String username, @NonNull final DatabaseCallback<Boolean> callback) {
        getAll(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getUserName(), username)) {
                        callback.onCompleted(true);
                        return;
                    }
                }
                callback.onCompleted(false);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Finds a user by username.
     * @param username username to search
     * @param callback matching user or null if not found
     */
    @Override
    public void findUserByUserName(@NonNull final String username, @NonNull final DatabaseCallback<User> callback) {
        getAll(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (Objects.equals(user.getUserName(), username)) {
                        callback.onCompleted(user); // מחזיר את המשתמש שנמצא
                        return;
                    }
                }
                callback.onCompleted(null); // אין משתמש כזה
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    /**
     * Returns the total number of registered users.
     * Used for admin dashboard and statistics.
     * @param callback number of users
     */
    @Override
    public void getUsersCount(@NonNull DatabaseCallback<Integer> callback) {
        getAll(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                callback.onCompleted(users.size());
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @Override
    public void isAdmin(@NonNull String userId,
                        @NonNull DatabaseCallback<Boolean> callback) {

        get(userId, new DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user == null) {
                    callback.onCompleted(false);
                    return;
                }

                callback.onCompleted(user.isAdmin());
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }
}