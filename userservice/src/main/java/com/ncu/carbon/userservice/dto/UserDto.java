package com.ncu.carbon.userservice.dto;

public class UserDto {
    private Long id;
    private String name;
    private Double credits;

    public UserDto() {}

    public UserDto(Long id, String name, Double credits) {
        this.id = id;
        this.name = name;
        this.credits = credits;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getCredits() { return credits; }
    public void setCredits(Double credits) { this.credits = credits; }
}
