package com.ncu.carbon.userservice.model;

public class User {
    private Long id;

    private String name;

    private double credits;

    private double balance;

    public User() {
    }

    public User(String name, double credits) {
        this.name = name;
        this.credits = credits;
        this.balance = 0.0;
    }

    public User(String name, double credits, double balance) {
        this.name = name;
        this.credits = credits;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
