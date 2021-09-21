package com.finance.loader.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public class AWSProps {

    private String messagingChannel;
    private String accessKey;
    private String accessSecretKey;
    private String region;

    public String getMessagingChannel() {
        return messagingChannel;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getAccessSecretKey() {
        return accessSecretKey;
    }

    public String getRegion() {
        return region;
    }
}
