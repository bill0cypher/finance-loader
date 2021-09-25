package com.finance.loader.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "iex")
public class IEXPropsHolder {

    private String apiToken;
    private String baseResource;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public void setBaseResource(String baseResource) {
        this.baseResource = baseResource;
    }

    public String getBaseResource() {
        return baseResource;
    }
}
