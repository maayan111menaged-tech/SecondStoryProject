package com.example.secondstoryproject.models;

public class Donation {

    public String id;
    public String name;
    public String description;
    public enum donationCategory{
        CLOTHES,
        TOYS,
        FOOD
    }



    public donationCategory category;
    public enum donationStatus{
        AVAILABLE,   // פנוי
        MATCHED,     // נמצא מישהו שצריך
        DELIVERED    // נמסר
    }
    public donationStatus status;
    public String photoUrl;
    public User giver;
    public User receiver;
    //public -- location;


    public Donation(String id, String name, String description, donationCategory category, donationStatus status, String photoUrl, User giver, User receiver) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.photoUrl = photoUrl;
        this.giver = giver;
        this.receiver = receiver;
    }
    public Donation() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public donationCategory getCategory() {
        return category;
    }

    public void setCategory(donationCategory category) {
        this.category = category;
    }

    public donationStatus getStatus() {
        return status;
    }

    public void setStatus(donationStatus status) {
        this.status = status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public User getGiver() {
        return giver;
    }

    public void setGiver(User giver) {
        this.giver = giver;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
}
