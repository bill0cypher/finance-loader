package com.finance.loader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.Date;
import java.util.List;

@Document(collection = "institution")
public class Institution {

    @Id
    private String iexId;
    private String symbol;
    private String exchange;
    private String exchangeSuffix;
    private String exchangeName;
    private String name;
    private Date date;
    private String type;
    private String region;
    private String currency;
    @JsonProperty(value = "isEnabled")
    @Field(name = "isEnabled", targetType = FieldType.BOOLEAN)
    private boolean isEnabled;
    private String figi;
    private String cik;
    private String lei;

    private List<StockInfoShortened> stockHistory;

    public Institution() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getExchangeSuffix() {
        return exchangeSuffix;
    }

    public void setExchangeSuffix(String exchangeSuffix) {
        this.exchangeSuffix = exchangeSuffix;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIexId() {
        return iexId;
    }

    public void setIexId(String iexId) {
        this.iexId = iexId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void isEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getFigi() {
        return figi;
    }

    public void setFigi(String figi) {
        this.figi = figi;
    }

    public String getCik() {
        return cik;
    }

    public void setCik(String cik) {
        this.cik = cik;
    }

    public String getLei() {
        return lei;
    }

    public void setLei(String lei) {
        this.lei = lei;
    }

    public StockInfoShortened getLastStockInfo() {
        return stockHistory != null ? stockHistory.get(stockHistory.size() - 1) : null;
    }
    public List<StockInfoShortened> getStockHistory() {
        return stockHistory;
    }

    public void setStockHistory(List<StockInfoShortened> stockHistory) {
        this.stockHistory = stockHistory;
    }

    @Override
    public String toString() {
        return "Institution: " + name + ": \n" +
                "Volume: " + getLastStockInfo().getVolume() + '\n' +
                "Prev. volume: " + getLastStockInfo().getPreviousVolume() + '\n' +
                "Change per-t: " + getLastStockInfo().getChangePercent() + '\n';
    }
}
