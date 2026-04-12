package com.example.secondstoryproject.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.example.secondstoryproject.models.User;
import com.google.gson.Gson;

/**
 * Utility class for managing SharedPreferences operations.
 * Provides methods to store, retrieve, and manage primitive values
 * and complex objects using JSON serialization (Gson).
 */
public class SharedPreferencesUtil {

    /** Name of the SharedPreferences file */
    private static final String PREF_NAME = "com.example.SecondStoryProject.PREFERENCE_FILE_KEY";

    /**
     * Saves a string value in SharedPreferences.
     * @param context the application context
     * @param key the key under which the value is stored
     * @param value the string value to save
     */
    private static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieves a string value from SharedPreferences.
     * @param context the application context
     * @param key the key of the stored value
     * @param defaultValue the value to return if the key does not exist
     * @return the stored string value, or defaultValue if not found
     */
    private static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Saves an integer value in SharedPreferences.
     * @param context the application context
     * @param key the key under which the value is stored
     * @param value the integer value to save
     */
    private static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Retrieves an integer value from SharedPreferences.
     * @param context the application context
     * @param key the key of the stored value
     * @param defaultValue the value to return if the key does not exist
     * @return the stored integer value, or defaultValue if not found
     */
    private static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    /**
     * Removes all stored data from SharedPreferences.
     * @param context the application context
     */
    public static void clear(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Removes a specific key from SharedPreferences.
     * @param context the application context
     * @param key the key to remove
     */
    private static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Checks whether a specific key exists in SharedPreferences.
     * @param context the application context
     * @param key the key to check
     * @return true if the key exists, false otherwise
     */
    private static boolean contains(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(key);
    }

    /**
     * Saves an object in SharedPreferences by converting it to JSON.
     * Uses Gson to serialize the object into a JSON string.
     * @param context the application context
     * @param key the key under which the object is stored
     * @param object the object to save
     * @param <T> the object type
     */
    private static <T> void saveObject(Context context, String key, T object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        saveString(context, key, json);
    }

    /**
     * Retrieves an object from SharedPreferences by converting it from JSON.
     * Uses Gson to deserialize the stored JSON string back into an object.
     * @param context the application context
     * @param key the key of the stored object
     * @param type the class type of the object
     * @param <T> the object type
     * @return the deserialized object, or null if not found
     */
    private static <T> T getObject(Context context, String key, Class<T> type) {
        String json = getString(context, key, null);
        if (json == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    /**
     * Saves a user object in SharedPreferences.
     * @param context the application context
     * @param user the user object to save
     */
    public static void saveUser(Context context, User user) {
        saveObject(context, "user", user);
    }

    /**
     * Retrieves the currently logged-in user.
     * @param context the application context
     * @return the user object, or null if no user is logged in
     */
    public static User getUser(Context context) {
        if (!isUserLoggedIn(context)) {
            return null;
        }
        return getObject(context, "user", User.class);
    }

    /**
     * Signs out the current user by removing stored user data.
     * @param context the application context
     */
    public static void signOutUser(Context context) {
        remove(context, "user");
    }

    /**
     * Checks whether a user is currently logged in.
     * @param context the application context
     * @return true if a user exists in SharedPreferences, false otherwise
     */
    public static boolean isUserLoggedIn(Context context) {
        return contains(context, "user");
    }

    /**
     * Retrieves the ID of the currently logged-in user.
     * @param context the application context
     * @return the user ID, or null if no user is logged in
     */
    @Nullable
    public static String getUserId(Context context) {
        User user = getUser(context);
        if (user != null) {
            return user.getId();
        }
        return null;
    }


}
