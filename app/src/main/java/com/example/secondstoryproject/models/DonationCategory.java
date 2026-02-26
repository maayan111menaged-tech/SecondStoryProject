package com.example.secondstoryproject.models;

import com.example.secondstoryproject.R;

public enum DonationCategory {

    CLOTHES("Clothes", "בגדים", R.drawable.ic_clothes),
    TOYS("Toys", "צעצועים", R.drawable.ic_toys),
    FOOD("Food", "מזון", R.drawable.ic_food),
    BOOKS("Books", "ספרים", R.drawable.ic_books),
    FURNITURE("Furniture", "ריהוט", R.drawable.ic_furniture),
    BABY_ITEMS("Baby Items", "ציוד לתינוקות", R.drawable.ic_baby),
    SCHOOL_SUPPLIES("School Supplies", "ציוד לבית ספר", R.drawable.ic_school),
    OTHER("Other","אחר",R.drawable.other);

    private final String englishName;
    private final String hebrewName;
    private final int iconResId;

    DonationCategory(String englishName, String hebrewName, int iconResId) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.iconResId = iconResId;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getHebrewName() {
        return hebrewName;
    }

    public int getIconResId() {
        return iconResId;
    }

    // לשימוש כששומרים כ-String ב-Firebase
    public static DonationCategory fromString(String value) {
        return DonationCategory.valueOf(value);
    }
}
