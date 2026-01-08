package com.example.secondstoryproject.models;

import com.example.secondstoryproject.R;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {

    private String id;
    private String userName;
    private String password;
    private String fName;
    private String lName;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private int donationCounter; //כמות התרומות שנתן
    private String profilePhoneUrl;
    private boolean isAdmin;
    private ArrayList<Donation> donationList; //יכלול גם תרומות שקיבל ושנתן
    private ArrayList<Rate> rateList; // פידבקים שקיבל

    public enum NotificationType {
        PHONE_NUMBER,
        EMAIL
    }
    private NotificationType notifications;

    // בנאי עבור ההרשמה
    public User(String id, String userName, String password,
                String fName, String lName,
                String email, String phoneNumber, String dateOfBirth) {

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;

        // ערכי ברירת מחדל
        this.donationCounter = 0;
        this.isAdmin = false;
        this.profilePhoneUrl = null;

        this.donationList = new ArrayList<>();
        this.rateList = new ArrayList<>();
        this.notifications = NotificationType.EMAIL;
    }

    //בנאי מלא
    public User(String id, String userName, String password,
                String fName, String lName,
                String email, String phoneNumber, String dateOfBirth,
                int donationCounter, boolean isAdmin, String profilePhoneUrl) {

        this.id = id;
        this.userName = userName;
        this.password = password;
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;

        this.donationCounter = donationCounter;
        this.isAdmin = isAdmin;
        this.profilePhoneUrl = profilePhoneUrl;

        this.donationList = new ArrayList<>();
        this.rateList = new ArrayList<>();
        this.notifications = NotificationType.EMAIL;
    }

    // בנאי ריק
    public User() {
        this.donationCounter = 0;
        this.isAdmin = false;

        this.donationList = new ArrayList<>();
        this.rateList = new ArrayList<>();
        this.notifications = NotificationType.EMAIL;
    }

    // GET & SET
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getfName() { return fName; }
    public void setfName(String fName) { this.fName = fName; }
    public String getlName() { return lName; }
    public void setlName(String lName) { this.lName = lName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public int getDonationCounter() { return donationCounter; }
    public void setDonationCounter(int donationCounter) {this.donationCounter = donationCounter;}
    public String getProfilePhoneUrl() { return profilePhoneUrl; }
    public void setProfilePhoneUrl(String profilePhoneUrl) {this.profilePhoneUrl = profilePhoneUrl;}
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public ArrayList<Donation> getDonationList() { return donationList; }
    public void setDonationList(ArrayList<Donation> donationList) {this.donationList = donationList;}
    public ArrayList<Rate> getRateList() { return rateList; }
    public void setRateList(ArrayList<Rate> rateList) {this.rateList = rateList;}
    public NotificationType getNotifications() { return notifications; }
    public void setNotifications(NotificationType notifications) {this.notifications = notifications;}

//    public String getCity() {return city;}
//    public void setCity(String city) {this.city = city;}



    // additional methods
    @Exclude
    public UserLevel getLevel() {
        return UserLevel.fromDonationCount(donationCounter);
    }

    @Exclude
    public String getFullName(){
        return this.fName + this.lName;
    }


}
