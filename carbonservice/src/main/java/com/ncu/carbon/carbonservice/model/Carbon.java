package com.ncu.carbon.carbonservice.model;

public class Carbon {
    private Long id;
    private String name;
    private Double supply;
    private Long ownerId;
    private Double price;

    public Carbon() {}

    public Carbon(Long id, String name, Double supply) {
        this.id = id;
        this.name = name;
        this.supply = supply;
    }

    public Carbon(Long id, String name, Double supply, Long ownerId, Double price) {
        this.id = id;
        this.name = name;
        this.supply = supply;
        this.ownerId = ownerId;
        this.price = price;
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

    public Double getSupply() {
        return supply;
    }

    public void setSupply(Double supply) {
        this.supply = supply;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
