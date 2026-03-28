package com.example.technova_be.modules.notification.service;

import com.example.technova_be.comom.constants.NotificationType;
import com.example.technova_be.comom.response.PageResponse;
import com.example.technova_be.modules.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    NotificationResponse sendNotification(Long userId, String title, String message, NotificationType type, String referenceId);

    PageResponse<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);
}
