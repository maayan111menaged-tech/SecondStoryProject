package com.example.secondstoryproject.models;

import java.util.ArrayList;

public class User {

    public String id;
    public String userName;
    public String password;
    public String fName;
    public String lName;
    public String email;
    public String phoneNumber;
    public String dateOfBirth;
    public int donationCounter;
    /// public rate  // דרגה נוכחית
    public String profilePhoneUrl;
    public boolean isAdmin;
    public ArrayList<Donation> donationList;
    public ArrayList<Rate> rateList;
    public enum NotificationType {
        PHONE_NUMBER,
        EMAIL
    }

    public NotificationType notifications;


    public User(String id, String userName, String password, String fName, String lName,
                String email, String phoneNumber,String dateOfBirth,
                int donationCounter, String profilePhoneUrl, boolean isAdmin) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        ///this.city = city;
        this.donationCounter = donationCounter;
        this.profilePhoneUrl = profilePhoneUrl;
        this.isAdmin = isAdmin;
        this.donationList = new ArrayList<>();
        this.rateList = new ArrayList<>();
        this.notifications = NotificationType.EMAIL;
    }

    public User() {
        this.donationList = new ArrayList<>();
        this.rateList = new ArrayList<>();
        this.notifications = NotificationType.EMAIL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

    public String getDateOfBirth() {return dateOfBirth;}

    public void setDateOfBirth(String dateOfBirth) {this.dateOfBirth = dateOfBirth;}


    //    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }

    public int getDonationCounter() {
        return donationCounter;
    }

    public void setDonationCounter(int donationCounter) {
        this.donationCounter = donationCounter;
    }

    public String getProfilePhoneUrl() {
        return profilePhoneUrl;
    }

    public void setProfilePhoneUrl(String profilePhoneUrl) {
        this.profilePhoneUrl = profilePhoneUrl;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public ArrayList<Donation> getDonationList() {
        return donationList;
    }

    public void setDonationList(ArrayList<Donation> donationList) {
        this.donationList = donationList;
    }

    public ArrayList<Rate> getRateList() {
        return rateList;
    }

    public void setRateList(ArrayList<Rate> rateList) {
        this.rateList = rateList;
    }
    public NotificationType getNotifications() {
        return notifications;
    }

    public void setNotifications(NotificationType notifications) {
        this.notifications = notifications;
    }



}
