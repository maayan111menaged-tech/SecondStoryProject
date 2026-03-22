package com.example.secondstoryproject.models;

import java.util.ArrayList;
import java.util.Date;

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

    private ArrayList<StatusLog> statusHistory; // היסטוריית סטטוסים



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

        this.statusHistory = new ArrayList<>();
        // מוסיפים את הסטטוס ההתחלתי להיסטוריה
        addStatusLog(status, null);


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


    public ArrayList<StatusLog> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(ArrayList<StatusLog> statusHistory) { this.statusHistory = statusHistory; }



    // ----- פונקציה לעדכון סטטוס -----
    /**
     * מעדכן את סטטוס התרומה ושומר את ההיסטוריה.
     *
     * @param newStatus הסטטוס החדש
     * @param reason    סיבה לשינוי, אם קיימת (יכול להיות null)
     */
    public void updateStatus(DonationStatus newStatus, String reason) {
        this.status = newStatus;
        addStatusLog(newStatus, reason);
    }

    // ----- פונקציה פנימית להוספת רשומה להיסטוריה -----
    private void addStatusLog(DonationStatus status, String reason) {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        statusHistory.add(new StatusLog(status, new Date(), reason));
    }

    // ----- StatusLog פנימי -----
    public static class StatusLog {
        private DonationStatus status;
        private Date timestamp;
        private String reason; // optional, לדוגמה דחייה עם סיבה

        public StatusLog() {
            // חובה ל-Firebase
        }

        public StatusLog(DonationStatus status, Date timestamp, String reason) {
            this.status = status;
            this.timestamp = timestamp;
            this.reason = reason;
        }

        public DonationStatus getStatus() { return status; }
        public void setStatus(DonationStatus status) { this.status = status; }

        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}