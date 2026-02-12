package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.taskflow.dto.response.NotificationResponse;
import com.taskflow.entity.Notification;
import com.taskflow.mapper.NotificationMapper;
import com.taskflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(Long userId, String type, String title, String content, Long referenceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReferenceId(referenceId);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public List<NotificationResponse> listNotifications(Long userId) {
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
        query.eq(Notification::getUserId, userId);
        query.orderByDesc(Notification::getCreatedAt);
        List<Notification> notifications = notificationMapper.selectList(query);

        return notifications.stream().map(this::toNotificationResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getId, id);
        updateWrapper.set(Notification::getIsRead, 1);
        notificationMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Notification::getUserId, userId);
        updateWrapper.eq(Notification::getIsRead, 0);
        updateWrapper.set(Notification::getIsRead, 1);
        notificationMapper.update(null, updateWrapper);
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
        query.eq(Notification::getUserId, userId);
        query.eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(query);
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getReferenceId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
