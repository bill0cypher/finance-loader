package com.finance.loader.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.finance.loader.component.AppProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class FinLoaderConfig {

    private final AppProperties appProperties;

    private ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("elastic")
                .pendingAcquireMaxCount(5000)
                .maxConnections(5000)
                .build();
    }

    private HttpClient createHttpClient() {
        return HttpClient.create(connectionProvider())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn
                                .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );
    }

    public AmazonSQSAsync sqsClient() {
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(
                appProperties.getAwsServiceEndpoint(),
                appProperties.getAwsRegion()
        );

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfig)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(appProperties.getAwsAccessKey(), appProperties.getAwsSecretKey())
                ))
                .build();
    }

    @Bean
    public WebClient createWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient()))
                .baseUrl(appProperties.getIexBaseResource())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", appProperties.getIexBaseResource()))
                .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync sqsAsync) {
        QueueMessagingTemplate messagingTemplate = new QueueMessagingTemplate(sqsClient());
        messagingTemplate.setDefaultDestination(new QueueMessageChannel(sqsClient(), appProperties.getAwsMessagingChannel()));
        return messagingTemplate;
    }
}
