package com.example.secondstoryproject.models;

import org.osmdroid.util.GeoPoint;

public enum IsraelCity {

    TEL_AVIV("Tel Aviv", "תל אביב", 32.0853, 34.7818),
    JERUSALEM("Jerusalem", "ירושלים", 31.7683, 35.2137),
    HAIFA("Haifa", "חיפה", 32.7940, 34.9896),
    BEER_SHEVA("Beer Sheva", "באר שבע", 31.2530, 34.7915),
    ASHDOD("Ashdod", "אשדוד", 31.8040, 34.6550),
    RISHON_LEZION("Rishon LeZion", "ראשון לציון", 31.9730, 34.7925),
    PETAH_TIKVA("Petah Tikva", "פתח תקווה", 32.0840, 34.8878),
    NETANYA("Netanya", "נתניה", 32.3215, 34.8532),
    RAMAT_GAN("Ramat Gan", "רמת גן", 32.0680, 34.8240),
    HOLON("Holon", "חולון", 32.0114, 34.7739),
    HERZLIYA("Herzliya", "הרצליה", 32.1663, 34.8436),
    KFAR_SABA("Kfar Saba", "כפר סבא", 32.1781, 34.9078),
    MODIIN("Modiin", "מודיעין", 31.8969, 35.0100),
    EILAT("Eilat", "אילת", 29.5577, 34.9519);

    private final String englishName;
    private final String hebrewName;
    private final double latitude;
    private final double longitude;

    IsraelCity(String englishName, String hebrewName, double latitude, double longitude) {
        this.englishName = englishName;
        this.hebrewName = hebrewName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEnglishName() { return englishName; }
    public String getHebrewName() { return hebrewName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

    // מחזיר רשימת שמות עבריים — לשימוש ב-Spinner/חיפוש
    public static String[] getHebrewNames() {
        IsraelCity[] cities = values();
        String[] names = new String[cities.length];
        for (int i = 0; i < cities.length; i++) {
            names[i] = cities[i].hebrewName;
        }
        return names;
    }

    // מחיפוש לפי שם עברי — לשימוש במפה
    public static IsraelCity fromHebrewName(String hebrewName) {
        for (IsraelCity city : values()) {
            if (city.hebrewName.equals(hebrewName)) {
                return city;
            }
        }
        return null;
    }

    public static IsraelCity fromString(String value) {
        return IsraelCity.valueOf(value);
    }
}