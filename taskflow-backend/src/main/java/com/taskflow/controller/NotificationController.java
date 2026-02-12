package com.taskflow.controller;

import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.NotificationResponse;
import com.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> listNotifications() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        List<NotificationResponse> response = notificationService.listNotifications(userId);
        return ApiResponse.success(response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        long count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        notificationService.markAllAsRead(userId);
        return ApiResponse.success();
    }
}
