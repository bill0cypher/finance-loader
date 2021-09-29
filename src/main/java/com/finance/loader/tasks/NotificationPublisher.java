package com.finance.loader.tasks;

import com.finance.loader.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class NotificationPublisher {

    private final QueueMessagingTemplate queueMessagingTemplate;

    public void sendNotification(NotificationDTO message) {
        queueMessagingTemplate.convertAndSend(message);
    }

    public void sendNotifications(List<NotificationDTO> messages) {
        queueMessagingTemplate.convertAndSend(messages);
    }
}
