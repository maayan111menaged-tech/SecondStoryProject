package com.example.secondstoryproject.models;

public class Donation implements Idable {

    private String id;
    private String name;
    private String description;
    private DonationCategory category;
    private DonationStatus status;
    private String photoUrl;
    private String city; // חדש – עיר

    private String giverID;
    private String receiverID;

    // Enum של סטטוס
    public enum DonationStatus {
        AVAILABLE,
        MATCHED,
        DELIVERED
    }

    public Donation() {
        // חובה ל-Firebase
    }

    public Donation(String id,
                    String name,
                    String description,
                    DonationCategory category,
                    DonationStatus status,
                    String photoUrl,
                    String city,
                    String giverID,
                    String receiverID) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.photoUrl = photoUrl;
        this.city = city;
        this.giverID = giverID;
        this.receiverID = receiverID;
    }

    // -------- Getters & Setters --------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DonationCategory getCategory() { return category; }
    public void setCategory(DonationCategory category) { this.category = category; }

    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getGiverID() { return giverID; }
    public void setGiverID(String giver) { this.giverID = giver; }

    public String getReceiverID() { return receiverID; }
    public void setReceiverID(String receiver) { this.receiverID = receiver; }
}