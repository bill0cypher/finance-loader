package com.finance.loader.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stockinfo")
@Getter
@Setter
@NoArgsConstructor
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
