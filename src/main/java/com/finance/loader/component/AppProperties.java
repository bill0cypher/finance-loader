package com.finance.loader.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Getter
@Slf4j
public class AppProperties {

    @Value("${iex.api-token}")
    private String iexApiToken;

    @Value("${iex.base-resource}")
    private String iexBaseResource;

    @Value("${aws.sqs.messaging_channel}")
    private String awsMessagingChannel;

    @Value("${aws.sqs.service-endpoint}")
    private String awsServiceEndpoint;

    @Value("${aws.access_key}")
    private String awsAccessKey;

    @Value("${aws.access_secret_key}")
    private String awsSecretKey;

    @Value("${aws.region}")
    private String awsRegion;
}
