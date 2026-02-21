package com.ncu.carbon.tradeservice.dto;

public class CarbonTradeDto {
    private Long carbonId;
    private Long buyerId;
    private double quantity;

    public CarbonTradeDto() {}

    public CarbonTradeDto(Long carbonId, Long buyerId, double quantity) {
        this.carbonId = carbonId;
        this.buyerId = buyerId;
        this.quantity = quantity;
    }

    public Long getCarbonId() {
        return carbonId;
    }

    public void setCarbonId(Long carbonId) {
        this.carbonId = carbonId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
