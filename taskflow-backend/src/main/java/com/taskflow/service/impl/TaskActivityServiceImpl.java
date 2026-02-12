package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.response.TaskActivityResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.TaskActivity;
import com.taskflow.entity.User;
import com.taskflow.mapper.TaskActivityMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityMapper taskActivityMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void logActivity(Long taskId, Long userId, String action, String fieldName, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setUserId(userId);
        activity.setAction(action);
        activity.setFieldName(fieldName);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setCreatedAt(LocalDateTime.now());
        taskActivityMapper.insert(activity);
    }

    @Override
    public List<TaskActivityResponse> getTaskActivities(Long taskId) {
        LambdaQueryWrapper<TaskActivity> query = new LambdaQueryWrapper<>();
        query.eq(TaskActivity::getTaskId, taskId);
        query.orderByDesc(TaskActivity::getCreatedAt);
        query.last("LIMIT 50");
        List<TaskActivity> activities = taskActivityMapper.selectList(query);
        return activities.stream().map(this::buildResponse).collect(Collectors.toList());
    }

    private TaskActivityResponse buildResponse(TaskActivity activity) {
        TaskActivityResponse response = new TaskActivityResponse();
        response.setId(activity.getId());
        response.setTaskId(activity.getTaskId());
        response.setAction(activity.getAction());
        response.setFieldName(activity.getFieldName());
        response.setOldValue(activity.getOldValue());
        response.setNewValue(activity.getNewValue());
        response.setCreatedAt(activity.getCreatedAt());

        if (activity.getUserId() != null) {
            User user = userMapper.selectById(activity.getUserId());
            if (user != null) {
                response.setUser(new UserResponse(
                        user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl()));
            }
        }

        return response;
    }
}
