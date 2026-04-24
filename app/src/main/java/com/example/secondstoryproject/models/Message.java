package com.example.secondstoryproject.models;

public class Message {
    private String id;
    private String senderId;
    private String text;
    private long timestamp;
    private boolean adminSender;

    public Message() {} // חובה ל-Firebase

    public Message(String id, String senderId, String text, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.adminSender = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isAdminSender() { return adminSender; }
    public void setAdminSender(boolean adminSender) { this.adminSender = adminSender; }
}