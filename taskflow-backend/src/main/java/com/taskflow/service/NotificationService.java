package com.taskflow.service;

import com.taskflow.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createNotification(Long userId, String type, String title, String content, Long referenceId);

    List<NotificationResponse> listNotifications(Long userId);

    void markAsRead(Long id);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);
}
