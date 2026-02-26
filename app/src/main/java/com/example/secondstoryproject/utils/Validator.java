package com.example.secondstoryproject.utils;
import android.content.Context;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.example.secondstoryproject.R;

import java.util.Date;
/// Validator class to validate user input.
/// This class contains static methods to validate user input,
/// like email, password, phone, name etc.
public class Validator {

    /// Check if the user name is valid
    /// @param  uName to validate
    /// @return true if the user name is valid, false otherwise
    /// allow - A to Z, a to z, 0 to 9, dot and underscore
    public static boolean isUNameValid(@Nullable String uName) {
        return uName != null && uName.matches("^[A-Za-z0-9._]{3,}$");
    }

    /// Check if the name is valid
    /// @param name name to validate
    /// @return true if the name is valid, false otherwise
    public static boolean isNameValid(@Nullable String name) {
        return name != null && name.length() >= 3;
    }

    /// Check if the email is valid
    /// @param email email to validate
    /// @return true if the email is valid, false otherwise
    /// @see Patterns#EMAIL_ADDRESS
    public static boolean isEmailValid(@Nullable String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /// Check if the phone number is valid
    /// @param phone phone number to validate
    /// @return true if the phone number is valid, false otherwise
    /// @see Patterns#PHONE
    public static boolean isPhoneValid(@Nullable String phone) {
        return phone != null && phone.length() >= 10 && Patterns.PHONE.matcher(phone).matches();
    }

    /// date


    /// Check if the password is valid
    /// @param password password to validate
    /// @return true if the password is valid, false otherwise
    public static boolean isPasswordValid(@Nullable String password) {
        return password != null && password.length() >= 6;
    }




    // =====================  שם תרומה =====================
    public static boolean isDonationNameValid(@Nullable String donationName) {
        return donationName != null && !donationName.trim().isEmpty();
    }

    // ===================== תיאור תרומה =====================
    public static boolean isDescriptionValid(@Nullable String description) {
        return description != null && description.trim().length() >= 20;
    }

    // ===================== עיר =====================
    // האם המשתמש הכניס משהו בכלל
    public static boolean isCityValid(@Nullable String city) {
        return city != null && !city.trim().isEmpty();
    }

    // האם העיר קיימת ברשימה (Context נדרש כדי לגשת ל-resources)
    public static boolean isCityInList(Context context, @Nullable String city) {
        if (city == null) return false;

        String[] cities = context.getResources().getStringArray(R.array.israel_cities);
        for (String c : cities) {
            if (c.equals(city)) {
                return true;
            }
        }
        return false;
    }

    // ===================== תאריך =====================
    // בדיקה שתאריך תפוגה בעתיד
    public static boolean isFutureDate(@Nullable Date date) {
        return date != null && date.after(new Date());
    }

}