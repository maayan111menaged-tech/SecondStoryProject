package com.example.secondstoryproject.models;

public class Rate{

    /** המשתמש שמקבל את הדירוג — התורם */
    private String ratedUserId;

    /** המשתמש שנותן את הדירוג — מקבל התרומה */
    private String ratingUserId;

    /** התרומה שעליה ניתן הדירוג */
    private String donationId;

    /** כמות הכוכבים (1-5) */
    private int starAmount;

    /** הערה טקסטואלית אופציונלית */
    private String comment;

    /** זמן שמירת הדירוג */
    private long timestamp;

    // חובה ל-Firebase
    public Rate() {}

    public Rate(String ratedUserId, String ratingUserId, String donationId,
                int starAmount, String comment) {
        this.ratedUserId = ratedUserId;
        this.ratingUserId = ratingUserId;
        this.donationId = donationId;
        this.starAmount = starAmount;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRatedUserId() { return ratedUserId; }
    public void setRatedUserId(String ratedUserId) { this.ratedUserId = ratedUserId; }

    public String getRatingUserId() { return ratingUserId; }
    public void setRatingUserId(String ratingUserId) { this.ratingUserId = ratingUserId; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public int getStarAmount() { return starAmount; }
    public void setStarAmount(int starAmount) { this.starAmount = starAmount; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }


}