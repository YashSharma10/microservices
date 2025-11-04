package com.ncu.carbon.userservice.dto;

public class UserDto {
    private Long id;
    private String name;
    private double credits;

    public UserDto() {}

    public UserDto(Long id, String name, double credits) {
        this.id = id;
        this.name = name;
        this.credits = credits;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }
}
