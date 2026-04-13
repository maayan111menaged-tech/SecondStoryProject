package com.example.secondstoryproject.services.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Idable;
import com.example.secondstoryproject.models.User;
import com.example.secondstoryproject.services.IDatabaseService.DatabaseCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;


/**
 * Abstract base service for Firebase Realtime Database operations.
 * Provides generic CRUD functionality for all entities in the application,
 * such as Users, Donations, and Chats.
 * Each specific service (e.g. UserService, DonationService) extends this class
 * and defines its own database path.
 * @param <T> entity type (must implement {@link Idable})
 */
public abstract class BaseFirebaseService<T extends Idable> {

    /** Tag used for logging */
    private static final String TAG = "BaseFirebaseService";

    /** Root reference to Firebase database */
    private final DatabaseReference databaseReference;

    /** Path of the entity in the database (e.g. "users", "donations", "chats") */
    private final String path;

    /** Class type for Firebase deserialization */
    private final Class<T> clazz;

    /**
     * Constructor.
     * @param path  database path for the entity
     * @param clazz class type of the entity
     */
    protected BaseFirebaseService(@NonNull final String path, @NonNull final Class<T> clazz) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
        this.path = path;
        this.clazz = clazz;
    }

    /**
     * Generates a unique ID for a new entity.
     * @return generated ID string
     */
    protected String generateId() {
        return databaseReference.child(path).push().getKey();
    }

    /**
     * Creates or overwrites an entity in the database.
     * @param item     entity to save
     * @param callback optional callback
     */
    protected void create(@NonNull final T item, @Nullable final DatabaseCallback<Void> callback) {
        writeData(path + "/" + item.getId(), item, callback);
    }

    /**
     * Retrieves a single entity by ID.
     * @param id       entity ID
     * @param callback result callback
     */
    protected void get(@NonNull final String id, @NonNull final DatabaseCallback<T> callback) {
        getData(path + "/" + id, callback);
    }

    /**
     * Retrieves all entities of this type.
     * @param callback result callback with list
     */
    protected void getAll(@NonNull final DatabaseCallback<List<T>> callback) {
        getDataList(path, callback);
    }

    /**
     * Deletes an entity by ID.
     * @param id       entity ID
     * @param callback optional callback
     */
    protected void delete(@NonNull final String id, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(path + "/" + id, callback);
    }


    /**
     * Updates an entity using a Firebase transaction.
     * @param id       entity ID
     * @param function transformation function
     * @param callback optional callback with updated entity
     */
    protected void update(@NonNull final String id, @NonNull final UnaryOperator<T> function, @Nullable final DatabaseCallback<T> callback) {
        runTransaction(path + "/" + id, function, callback);
    }

    // ===================== Internal helpers =====================

    private DatabaseReference readData(@NonNull final String fullPath) {
        return databaseReference.child(fullPath);
    }

    private void writeData(@NonNull final String fullPath, @NonNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(fullPath).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private void deleteData(@NonNull final String fullPath, @Nullable final DatabaseCallback<Void> callback) {
        readData(fullPath).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    private void getData(@NonNull final String fullPath, @NonNull final DatabaseCallback<T> callback) {
        readData(fullPath).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private void getDataList(@NonNull final String fullPath, @NonNull final DatabaseCallback<List<T>> callback) {
        readData(fullPath).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });
            callback.onCompleted(tList);
        });
    }


    private void runTransaction(@NonNull final String fullPath, @NonNull final UnaryOperator<T> function, @Nullable final DatabaseCallback<T> callback) {
        readData(fullPath).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // bug note: currentValue can be null even if the data exists in the database.
                // Firebase will then re-run the transaction with the correct data.
                T currentValue = currentData.getValue(clazz);
                if (currentValue != null) {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    if (callback != null) {
                        callback.onFailed(error.toException());
                    }
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                if (callback != null) {
                    callback.onCompleted(result);
                }
            }
        });
    }

    protected DatabaseReference getRootRef() {
        return databaseReference;
    }
}