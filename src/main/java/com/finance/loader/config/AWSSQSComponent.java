package com.finance.loader.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.finance.loader.props.AWSProps;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AWSSQSComponent {

    private final AWSProps awsProps;

    public AWSSQSComponent(AWSProps awsProps) {
        this.awsProps = awsProps;
    }

    public AmazonSQSAsync sqsClient() {
        AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(
                "sqs.us-east-1.amazonaws.com",
                awsProps.getRegion()
        );

        return AmazonSQSAsyncClientBuilder
                .standard()
                .withEndpointConfiguration(endpointConfig)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(awsProps.getAccessKey(), awsProps.getAccessSecretKey())
                ))
                .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync sqsAsync) {
        QueueMessagingTemplate messagingTemplate = new QueueMessagingTemplate(sqsClient());
        messagingTemplate.setDefaultDestination(new QueueMessageChannel(sqsAsync, awsProps.getMessagingChannel()));
        return messagingTemplate;
    }
}
