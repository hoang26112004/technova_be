package com.example.technova_be.modules.notification.dto;

import com.example.technova_be.comom.constants.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class NotificationResponse {
    Long id;
    String title;
    String message;
    NotificationType type;
    String referenceId;
    boolean read;
    Instant createdAt;
}
