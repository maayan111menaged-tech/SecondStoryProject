package com.example.secondstoryproject.utils;
import android.content.Context;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.example.secondstoryproject.R;
import com.example.secondstoryproject.models.IsraelCity;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;


/**
 * Provides utility methods for validating user input across the application.
 * Includes validation for:
 * - User credentials (username, password, email, phone)
 * - Donation details (name, description, city, expiration date)
 */
public class Validator {

    /**
     * Validates a username.
     * Allows letters (A-Z, a-z), digits (0-9), dot (.), and underscore (_).
     * Minimum length: 3 characters.
     * @param uName the username to validate
     * @return true if valid, false otherwise
     */
    public static boolean isUNameValid(@Nullable String uName) {
        return uName != null && uName.matches("^[A-Za-z0-9._]{3,}$");
    }

    /**
     * Validates a person's name.
     * Minimum length: 3 characters.
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNameValid(@Nullable String name) {
        return name != null && name.length() >= 3;
    }

    /**
     * Validates an email address using Android built-in patterns.
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isEmailValid(@Nullable String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates a phone number using Android built-in patterns.
     * Minimum length: 10 digits.
     * @param phone the phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPhoneValid(@Nullable String phone) {
        return phone != null && phone.length() >= 10 && Patterns.PHONE.matcher(phone).matches();
    }

    /// date


    /**
     * Validates a password.
     * Minimum length: 6 characters.
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isPasswordValid(@Nullable String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validates a donation name.
     * Must not be null or empty.
     * @param donationName the donation name to validate
     * @return true if valid, false otherwise
     */    public static boolean isDonationNameValid(@Nullable String donationName) {
        return donationName != null && !donationName.trim().isEmpty();
    }

    /**
     * Validates a donation description.
     * Minimum length: 20 characters (after trimming).
     * @param description the description to validate
     * @return true if valid, false otherwise
     */    public static boolean isDescriptionValid(@Nullable String description) {
        return description != null && description.trim().length() >= 20;
    }

    /**
     * Checks if a city string is not empty.
     * @param city the city to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isCityValid(@Nullable String city) {
        return city != null && !city.trim().isEmpty();
    }

    /**
     * Checks whether the given city exists in the predefined list of Israeli cities.
     * @param context the application context (reserved for future use if needed)
     * @param city the city to validate
     * @return true if the city exists in the list, false otherwise
     */
     public static boolean isCityInList(Context context, @Nullable String city) {
        if (city == null) return false;

        String[] cities = IsraelCity.getHebrewNames();
        for (String c : cities) {
            if (c.equals(city)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates a date of birth string (format: dd/MM/yyyy).
     * Must be a valid date in the past (not in the future).
     * @param dateStr the date string to validate
     * @return true if valid and in the past, false otherwise
     */
    public static boolean isBirthDateValid(@Nullable String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date date = sdf.parse(dateStr);
            return date != null && date.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
