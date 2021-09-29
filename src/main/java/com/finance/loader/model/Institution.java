package com.finance.loader.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.Date;
import java.util.List;

@Document(collection = "institution")
@Getter
@Setter
public class Institution {

    @Id
    private String iexId;
    private String name;
    private String symbol;
    @JsonProperty(value = "isEnabled")
    @Field(name = "isEnabled", targetType = FieldType.BOOLEAN)
    private boolean isEnabled;

    private List<StockInfoShortened> stockHistory;

    public Institution() {
    }

    public StockInfoShortened getLastStockInfo() {
        return stockHistory != null ? stockHistory.get(stockHistory.size() - 1) : null;
    }

    @Override
    public String toString() {
        return "Institution: " + name + ": \n" +
                "Volume: " + getLastStockInfo().getVolume() + '\n' +
                "Prev. volume: " + getLastStockInfo().getPreviousVolume() + '\n' +
                "Change per-t: " + getLastStockInfo().getChangePercent() + '\n';
    }
}
