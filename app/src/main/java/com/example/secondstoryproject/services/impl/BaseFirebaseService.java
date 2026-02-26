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


/// abstract base class providing generic CRUD operations for Firebase Realtime Database.
/// @param <T> the type of the entity, must implement {@link Idable}
/// @see Idable
/// @see DatabaseReference
public abstract class BaseFirebaseService<T extends Idable> {

    /// tag for logging
    private static final String TAG = "BaseFirebaseService";


    /// the reference to the database
    private final DatabaseReference databaseReference;

    /// the path in the database for this entity type
    private final String path;

    /// the class of the entity type (needed for Firebase deserialization)
    private final Class<T> clazz;

    /// @param path the path in the database for this entity type (e.g. "users", "foods", "carts")
    /// @param clazz the class of the entity type
    protected BaseFirebaseService(@NonNull final String path, @NonNull final Class<T> clazz) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();
        this.path = path;
        this.clazz = clazz;
    }

    /// generate a new id for a new entity in the database
    /// @return a new id
    protected String generateId() {
        return databaseReference.child(path).push().getKey();
    }

    /// create or overwrite an entity in the database
    /// @param item the entity to create
    /// @param callback the callback to call when the operation is completed
    protected void create(@NonNull final T item, @Nullable final DatabaseCallback<Void> callback) {
        writeData(path + "/" + item.getId(), item, callback);
    }

    /// get a single entity from the database by id
    /// @param id the id of the entity
    /// @param callback the callback to call when the operation is completed
    protected void get(@NonNull final String id, @NonNull final DatabaseCallback<T> callback) {
        getData(path + "/" + id, callback);
    }

    /// get all entities of this type from the database
    /// @param callback the callback to call when the operation is completed
    protected void getAll(@NonNull final DatabaseCallback<List<T>> callback) {
        getDataList(path, callback);
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
    /// delete an entity from the database by id
    /// @param id the id of the entity to delete
    /// @param callback the callback to call when the operation is completed
    protected void delete(@NonNull final String id, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(path + "/" + id, callback);
    }

    /// update an entity using a transaction
    /// @param id the id of the entity to update
    /// @param function the function to apply to the current value
    /// @param callback the callback to call when the operation is completed
    protected void update(@NonNull final String id, @NonNull final UnaryOperator<T> function, @Nullable final DatabaseCallback<T> callback) {
        runTransaction(path + "/" + id, function, callback);
    }

    // region low-level helpers

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

        // endregion low-level helpers
}