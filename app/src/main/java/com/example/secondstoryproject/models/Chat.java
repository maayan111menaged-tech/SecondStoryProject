package com.example.secondstoryproject.models;

public class Chat {
    private String id;
    private String type;        // "donation" או "admin"
    private String donationId;  // רלוונטי רק לסוג donation
    private String giverId;
    private String receiverId;
    private String lastMessage;
    private long lastTimestamp;

    private int unreadCount; // לא נשמר ב-DB, רק בזיכרון


    private String donationName;    // שם התרומה
    private String otherUserName;   // שם המשתמש השני
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

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getDonationName() { return donationName; }
    public void setDonationName(String donationName) { this.donationName = donationName; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }


}