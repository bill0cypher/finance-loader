package com.finance.loader.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class InstitutionDTO {

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

}
