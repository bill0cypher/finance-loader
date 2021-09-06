package com.finance.loader.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stockinfo")
public class StockInfoShortened {

    @Id
    private String symbol;
    private String companyName;
    private String currency;
    private Double volume;
    private Double previousVolume;
    private Double latestVolume;
    private Integer avgTotalVolume;
    private Double change;
    private Double changePercent;

    public StockInfoShortened() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getPreviousVolume() {
        return previousVolume;
    }

    public void setPreviousVolume(Double previousVolume) {
        this.previousVolume = previousVolume;
    }

    public Double getLatestVolume() {
        return latestVolume;
    }

    public void setLatestVolume(Double latestVolume) {
        this.latestVolume = latestVolume;
    }

    public Integer getAvgTotalVolume() {
        return avgTotalVolume;
    }

    public void setAvgTotalVolume(Integer avgTotalVolume) {
        this.avgTotalVolume = avgTotalVolume;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(Double changePercent) {
        this.changePercent = changePercent;
    }

    @Override
    public String toString() {
        return "StockInfoShortened{" +
                "companyName='" + companyName + '\'' +
                ", currency='" + currency + '\'' +
                ", volume=" + volume +
                ", changePercent=" + changePercent +
                '}';
    }
}
