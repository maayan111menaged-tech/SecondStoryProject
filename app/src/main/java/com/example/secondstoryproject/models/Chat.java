package com.example.secondstoryproject.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Chat {
    private String id;
    private String type;        // "donation" או "admin"
    private String donationId;  // רלוונטי רק לסוג donation
    private String giverId;
    private String receiverId;
    private String lastMessage;
    private long lastTimestamp;

    @Exclude
    private int unreadCount; // לא נשמר ב-DB, רק בזיכרון

    @Exclude
    private String donationName;    // שם התרומה
    @Exclude
    private String otherUserName;   // שם המשתמש השני
    @Exclude
    private String otherUserId;     // ID של המשתמש השני

    public Chat() {} // חובה ל-Firebase

    public Chat(String id, String type, String donationId,
                String giverId, String receiverId) {
        this.id = id;
        this.type = type;
        this.donationId = donationId;
        this.giverId = giverId;
        this.receiverId = receiverId;
        this.lastMessage = "";
        this.lastTimestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }
    public String getGiverId() { return giverId; }
    public void setGiverId(String giverId) { this.giverId = giverId; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }

    @Exclude
    public int getUnreadCount() { return unreadCount; }
    @Exclude
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    @Exclude
    public String getDonationName() { return donationName; }
    @Exclude
    public void setDonationName(String donationName) { this.donationName = donationName; }
    @Exclude
    public String getOtherUserName() { return otherUserName; }
    @Exclude
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    @Exclude
    public String getOtherUserId() { return otherUserId; }
    @Exclude
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }


}