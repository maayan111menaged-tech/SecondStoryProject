package com.example.secondstoryproject.models;

public class Rate {

    public User giver;
    public User receiver;
    public int starAmount;

    public Rate(User giver, User receiver, int starAmount) {
        this.giver = giver;
        this.receiver = receiver;
        this.starAmount = starAmount;
    }
    public Rate() {}

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

    public int getStarAmount() {
        return starAmount;
    }

    public void setStarAmount(int starAmount) {
        this.starAmount = starAmount;
    }
}


