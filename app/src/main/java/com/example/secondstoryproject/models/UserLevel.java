package com.example.secondstoryproject.models;

import com.example.secondstoryproject.R;

public enum UserLevel {

    BRONZE("תורם ארד", 0, 4, R.drawable.ic_bronze),
    SILVER("תורם כסף", 5, 14, R.drawable.ic_silver),
    GOLD("תורם זהב", 15, 29, R.drawable.ic_gold),
    TROPHY("תורם מצטיין", 30, 49, R.drawable.ic_trophy),
    CROWN("גיבור הקהילה", 50, 99, R.drawable.ic_crown),
    EARTH("שגריר הנתינה", 100, Integer.MAX_VALUE, R.drawable.ic_earth);

    private final String label;
    private final int minDonations;
    private final int maxDonations;
    private final int iconRes;

    UserLevel(String label, int minDonations, int maxDonations, int iconRes) {
        this.label = label;
        this.minDonations = minDonations;
        this.maxDonations = maxDonations;
        this.iconRes = iconRes;
    }
    public int getMinDonations() {
        return minDonations;
    }

    public int getMaxDonations() {
        return maxDonations;
    }

    public String getLabel() {
        return label;
    }

    public int getIconRes() {
        return iconRes;
    }

    public static UserLevel fromDonationCount(int donationCounter) {
        for (UserLevel level : values()) {
            if (donationCounter >= level.minDonations &&
                    donationCounter <= level.maxDonations) {
                return level;
            }
        }
        return BRONZE;
    }
}
