package com.finance.loader.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "aws")
public class AWSProps {

    private String accessKey;
    private String accessSecretKey;
    private String region;
    @NestedConfigurationProperty
    private SQSProps sqs;
    @NestedConfigurationProperty
    private SNSProps sns;


    public static class SQSProps {
        private String messagingChannel;

        public String getMessagingChannel() {
            return messagingChannel;
        }

        public void setMessagingChannel(String messagingChannel) {
            this.messagingChannel = messagingChannel;
        }
    }

    public static class SNSProps {
        private String topicArn;

        public String getTopicArn() {
            return topicArn;
        }

        public void setTopicArn(String topicArn) {
            this.topicArn = topicArn;
        }
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

    public SQSProps getSqs() {
        return sqs;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setAccessSecretKey(String accessSecretKey) {
        this.accessSecretKey = accessSecretKey;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setSqs(SQSProps sqs) {
        this.sqs = sqs;
    }

    public SNSProps getSns() {
        return sns;
    }

    public void setSns(SNSProps sns) {
        this.sns = sns;
    }
}
