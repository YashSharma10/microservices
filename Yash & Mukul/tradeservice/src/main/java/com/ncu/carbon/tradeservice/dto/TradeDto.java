package com.ncu.carbon.tradeservice.dto;

public class TradeDto {
    private Long from;
    private Long to;
    private double amount;

    public TradeDto() {}

    public TradeDto(Long from, Long to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Long getFrom() { return from; }
    public void setFrom(Long from) { this.from = from; }

    public Long getTo() { return to; }
    public void setTo(Long to) { this.to = to; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
