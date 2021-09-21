package com.finance.loader.tasks;

import com.finance.loader.dto.Notification;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.QueueMessageChannel;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NotificationPublisher {

    private final QueueMessagingTemplate queueMessagingTemplate;

    public NotificationPublisher(QueueMessagingTemplate queueMessagingTemplate) {
        this.queueMessagingTemplate = queueMessagingTemplate;
    }

    public void send(Notification message, String subject) {
        queueMessagingTemplate.convertAndSend(message);
    }

    public void sendMany(List<Notification> messages, String subject) {
        queueMessagingTemplate.convertAndSend(messages);
    }
}
