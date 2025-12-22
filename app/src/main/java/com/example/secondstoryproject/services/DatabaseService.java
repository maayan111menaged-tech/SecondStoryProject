package com.example.secondstoryproject.services;
// line 391 start of cart region - unused
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.Donation;
import com.example.secondstoryproject.models.User;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/// a service to interact with the Firebase Realtime Database.
/// this class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase
public class DatabaseService {

    /// tag for logging
    /// @see Log
    private static final String TAG = "DatabaseService";

    /// paths for different data types in the database
    /// @see DatabaseService#readData(String)
    private static final String USERS_PATH = "users",
            DONATION_PATH = "Donation" ; //,
            //CARTS_PATH = "carts";

    /// callback interface for database operations
    /// @param <T> the type of the object to return
    /// @see DatabaseCallback#onCompleted(Object)
    /// @see DatabaseCallback#onFailed(Exception)
    public interface DatabaseCallback<T> {
        /// called when the operation is completed successfully
        public void onCompleted(T object);

        /// called when the operation fails with an exception
        public void onFailed(Exception e);
    }

    /// the instance of this class
    /// @see #getInstance()
    private static DatabaseService instance;

    /// the reference to the database
    /// @see DatabaseReference
    /// @see FirebaseDatabase#getReference()
    private final DatabaseReference databaseReference;

    /// use getInstance() to get an instance of this class
    /// @see DatabaseService#getInstance()
    private DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://second-story-33031-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();
    }

    /// get an instance of this class
    /// @return an instance of this class
    /// @see DatabaseService
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }


    // region private generic methods
    // to write and read data from the database

    /// write data to the database at a specific path
    /// @param path the path to write the data to
    /// @param data the data to write (can be any object, but must be serializable, i.e. must have a default constructor and all fields must have getters and setters)
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /// remove data from the database at a specific path
    /// @param path the path to remove the data from
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback == null) return;
                callback.onFailed(error.toException());
            } else {
                if (callback == null) return;
                callback.onCompleted(null);
            }
        });
    }

    /// read data from the database at a specific path
    /// @param path the path to read the data from
    /// @return a DatabaseReference object to read the data from
    /// @see DatabaseReference

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }


    /// get data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the object to return
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseCallback
    /// @see Class
    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    /// get a list of data from the database at a specific path
    /// @param path the path to get the data from
    /// @param clazz the class of the objects to return
    /// @param callback the callback to call when the operation is completed
    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
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

    /// generate a new id for a new object in the database
    /// @param path the path to generate the id for
    /// @return a new id for the object
    /// @see String
    /// @see DatabaseReference#push()

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }


    /// run a transaction on the data at a specific path </br>
    /// good for incrementing a value or modifying an object in the database
    /// @param path the path to run the transaction on
    /// @param clazz the class of the object to return
    /// @param function the function to apply to the current value of the data
    /// @param callback the callback to call when the operation is completed
    /// @see DatabaseReference#runTransaction(Transaction.Handler)
    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                if (currentValue == null) {
                    currentValue = function.apply(null);
                } else {
                    currentValue = function.apply(currentValue);
                }
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });

    }

    // endregion of private methods for reading and writing data

    // public methods to interact with the database

    // region User Section

    /// generate a new id for a new user in the database
    /// @return a new id for the user
    /// @see #generateNewId(String)
    /// @see User
    public String generateUserId() {
        return generateNewId(USERS_PATH);
    }

    /// create a new user in the database
    /// @param user the user object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void writeUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        writeData(USERS_PATH + "/" + user.getId(), user, callback);
    }

    /// get a user from the database
    /// @param uid the id of the user to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the user object
    ///             if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    /// get all the users from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of user objects
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see List
    /// @see User
    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    /// delete a user from the database
    /// @param uid the user id to delete
    /// @param callback the callback to call when the operation is completed
    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    /// get a user by email and password
    /// @param username the user name of the user
    /// @param password the password of the user
    /// @param callback the callback to call when the operation is completed
    ///            the callback will receive the user object
    ///          if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see User
    public void getUserByUserNameAndPassword(@NotNull final String username, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        Log.d(TAG, "in getUserByUserNameAndPassword function");
        getUserList(new DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                for (User user : users) {
                    if (user.getUserName().equals(username) && user.getPassword().equals(password)) {
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

    /// check if an email already exists in the database
    /// @param username the email to check
    /// @param callback the callback to call when the operation is completed
    public void checkIfUserNameExists(@NotNull final String username, @NotNull final DatabaseCallback<Boolean> callback) {
        Log.d(TAG, "in function checkIfUserNameExists");
        readData(USERS_PATH).orderByChild("userName").equalTo(username).get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG,"after");
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                        callback.onFailed(task.getException());
                        return;
                    }
                    boolean exists = task.getResult().getChildrenCount() > 0;
                    Log.d(TAG, "Username exists? " + exists);
                    callback.onCompleted(exists);
                });
    }
    public void findUserByUserName(@NotNull final String username, @NotNull final DatabaseCallback<User> callback) {
        Log.d(TAG, "🔍 findUserByUserName called with username: " + username);

        readData(USERS_PATH).orderByChild("userName").equalTo(username).get()
                .addOnCompleteListener(task ->
                {

                    if (!task.isSuccessful()) {
                        Log.d(TAG, "❌ Firebase task failed", task.getException());
                        callback.onFailed(task.getException());
                        return;
                    }

                    if (task.getResult() == null) {
                        Log.d(TAG, "⚠️ Task result is null");
                        callback.onCompleted(null);
                        return;
                    }

                    long childrenCount = task.getResult().getChildrenCount();
                    Log.d(TAG, "ℹ️ Task completed successfully, children count: " + childrenCount);

                    if (childrenCount == 0) {
                        Log.d(TAG, "❌ No user found with username: " + username);
                        callback.onCompleted(null);
                        return;
                    }

                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        Log.d(TAG, "✅ Found child key: " + dataSnapshot.getKey());

                        User user = dataSnapshot.getValue(User.class);
                        if (user == null) {
                            Log.e(TAG, "⚠️ Failed to map DataSnapshot to User object");
                            continue; // מנסה את השאר אם יש יותר children
                        } else {
                            Log.d(TAG, "✅ User mapped successfully: " + user.getUserName() + ", email: " + user.getEmail());
                        }

                        callback.onCompleted(user);
                        return;
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firebase query failed with exception", e);
                    callback.onFailed(e);
                });
    }


    // endregion User Section

    // region food section

    /// create a new food in the database
    /// @param donation the donation object to create
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive void
    ///             if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see Donation
    public void createNewDonation(@NotNull final Donation donation, @Nullable final DatabaseCallback<Void> callback) {
        writeData(DONATION_PATH + "/" + donation.getId(), donation, callback);
    }
    /// get a food from the database
    /// @param foodId the id of the food to get
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive the food object
    ///              if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see Donation
    public void getDonation(@NotNull final String foodId, @NotNull final DatabaseCallback<Donation> callback) {
        getData(DONATION_PATH + "/" + foodId, Donation.class, callback);
    }

    /// get all the foods from the database
    /// @param callback the callback to call when the operation is completed
    ///              the callback will receive a list of food objects
    ///            if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see List
    /// @see Donation
    public void getDonationList(@NotNull final DatabaseCallback<List<Donation>> callback) {
        getDataList(DONATION_PATH, Donation.class, callback);
    }

    /// generate a new id for a new food in the database
    /// @return a new id for the food
    /// @see #generateNewId(String)
    /// @see Donation
    public String generateDonationId() {
        return generateNewId(DONATION_PATH);
    }

    /// delete a food from the database
    /// @param DonationId the id of the food to delete
    /// @param callback the callback to call when the operation is completed
    public void deleteDonation(@NotNull final String DonationId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(DONATION_PATH + "/" + DonationId, callback);
    }

    // endregion food section
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // region cart section

    /// create a new cart in the database
    /// @param cart the cart object to create
    /// @param callback the callback to call when the operation is completed
    ///               the callback will receive void
    ///              if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see Cart
//    public void createNewCart(@NotNull final Cart cart, @Nullable final DatabaseCallback<Void> callback) {
//        writeData(CARTS_PATH + "/" + cart.getId(), cart, callback);
//    }

    /// get a cart from the database
    /// @param cartId the id of the cart to get
    /// @param callback the callback to call when the operation is completed
    ///                the callback will receive the cart object
    ///               if the operation fails, the callback will receive an exception
    /// @see DatabaseCallback
    /// @see Cart
//    public void getCart(@NotNull final String cartId, @NotNull final DatabaseCallback<Cart> callback) {
//        getData(CARTS_PATH + "/" + cartId, Cart.class, callback);
//    }
//
//    /// get all the carts from the database
//    /// @param callback the callback to call when the operation is completed
//    ///               the callback will receive a list of cart objects
//    ///
//    public void getCartList(@NotNull final DatabaseCallback<List<Cart>> callback) {
//        getDataList(CARTS_PATH, Cart.class, callback);
//    }
//
//    /// get all the carts of a specific user from the database
//    /// @param uid the id of the user to get the carts for
//    /// @param callback the callback to call when the operation is completed
//    public void getUserCartList(@NotNull String uid, @NotNull final DatabaseCallback<List<Cart>> callback) {
//        getCartList(new DatabaseCallback<>() {
//            @Override
//            public void onCompleted(List<Cart> carts) {
//                carts.removeIf(cart -> !Objects.equals(cart.getUid(), uid));
//                callback.onCompleted(carts);
//            }
//
//            @Override
//            public void onFailed(Exception e) {
//                callback.onFailed(e);
//            }
//        });
//    }
//
//
//    /// generate a new id for a new cart in the database
//    /// @return a new id for the cart
//    /// @see #generateNewId(String)
//    /// @see Cart
//    public String generateCartId() {
//        return generateNewId(CARTS_PATH);
//    }
//
//    /// delete a cart from the database
//    /// @param cartId the id of the cart to delete
//    /// @param callback the callback to call when the operation is completed
//    public void deleteCart(@NotNull final String cartId, @Nullable final DatabaseCallback<Void> callback) {
//        deleteData(CARTS_PATH + "/" + cartId, callback);
//    }
//
//    // endregion cart section

}