package com.taskflow.websocket;

import com.taskflow.dto.response.NotificationResponse;
import com.taskflow.dto.response.TaskResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskWebSocketHandler {
    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastTaskUpdate(Long workspaceId, TaskResponse task) {
        messagingTemplate.convertAndSend("/topic/workspace/" + workspaceId + "/tasks", task);
    }

    public void broadcastNotification(Long userId, NotificationResponse notification) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", notification);
    }
}
