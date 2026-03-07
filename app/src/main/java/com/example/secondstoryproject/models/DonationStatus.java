package com.example.secondstoryproject.models;

import com.example.secondstoryproject.R;

public enum DonationStatus {

    PENDING_APPROVAL("PENDING_APPROVAL", "ממתין לאישור", R.drawable.ic_pending),
    UNDER_REVIEW("UNDER_REVIEW", "בבדיקה", R.drawable.ic_review),

    APPROVED_AVAILABLE("APPROVED_AVAILABLE", "זמין לתרומה", R.drawable.ic_available),

    MATCHED("MATCHED", "נמצא מקבל", R.drawable.ic_matched),
    IN_DELIVERY("IN_DELIVERY", "בדרך למסירה", R.drawable.ic_delivery),

    DELIVERED("DELIVERED", "נמסר", R.drawable.ic_delivered),

    REJECTED("REJECTED", "נדחה", R.drawable.ic_rejected),
    CANCELLED("CANCELLED", "בוטל", R.drawable.ic_cancelled),

    EXPIRED("EXPIRED", "פג תוקף", R.drawable.ic_expired);

    // ---- שדות ----
    private final String englishName;
    private final String hebrewName;
    private final int iconResId;

    // ---- Constructor ----
    DonationStatus(String englishName, String hebrewName, int iconResId) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.iconResId = iconResId;
    }

    // ---- Getters ----
    public String getEnglishName() {
        return englishName;
    }

    public String getHebrewName() {
        return hebrewName;
    }

    public int getIconResId() {
        return iconResId;
    }

    // ---- שימוש מ-Firebase ----
    public static DonationStatus fromString(String value) {
        return DonationStatus.valueOf(value);
    }

    // ---- פונקציות עזר ----

    // האם התרומה עדיין פעילה במערכת
    public boolean isActive() {
        return this == PENDING_APPROVAL ||
                this == UNDER_REVIEW ||
                this == APPROVED_AVAILABLE ||
                this == MATCHED ||
                this == IN_DELIVERY;
    }

    // האם התרומה הסתיימה (לא פעילה)
    public boolean isFinished() {
        return this == DELIVERED ||
                this == REJECTED ||
                this == CANCELLED ||
                this == EXPIRED;
    }

    // האם התרומה זמינה למקבל
    public boolean isAvailableForReceiver() {
        return this == APPROVED_AVAILABLE;
    }

    // האם אחראי צריך לטפל בתרומה
    public boolean needsManagerAction() {
        return this == PENDING_APPROVAL || this == UNDER_REVIEW;
    }


}