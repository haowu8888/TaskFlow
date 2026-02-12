package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.request.TaskDependencyCreateRequest;
import com.taskflow.dto.response.SubtaskResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Subtask;
import com.taskflow.entity.Task;
import com.taskflow.entity.TaskDependency;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.CommentMapper;
import com.taskflow.mapper.SubtaskMapper;
import com.taskflow.mapper.TaskDependencyMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.TaskDependencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskDependencyServiceImpl implements TaskDependencyService {

    private final TaskDependencyMapper taskDependencyMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final SubtaskMapper subtaskMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public void addDependency(Long taskId, TaskDependencyCreateRequest request) {
        Long successorId = request.getSuccessorTaskId() != null ? request.getSuccessorTaskId() : taskId;

        // Check for duplicate dependency
        LambdaQueryWrapper<TaskDependency> query = new LambdaQueryWrapper<>();
        query.eq(TaskDependency::getPredecessorTaskId, request.getPredecessorTaskId());
        query.eq(TaskDependency::getSuccessorTaskId, successorId);
        if (taskDependencyMapper.selectCount(query) > 0) {
            throw new BusinessException("This dependency already exists");
        }

        TaskDependency dependency = new TaskDependency();
        dependency.setPredecessorTaskId(request.getPredecessorTaskId());
        dependency.setSuccessorTaskId(successorId);
        dependency.setDependencyType(request.getDependencyType() != null ? request.getDependencyType() : "FINISH_TO_START");
        dependency.setCreatedAt(LocalDateTime.now());
        taskDependencyMapper.insert(dependency);
    }

    @Override
    @Transactional
    public void removeDependency(Long taskId, Long predecessorId, Long successorId) {
        LambdaQueryWrapper<TaskDependency> query = new LambdaQueryWrapper<>();
        query.eq(TaskDependency::getPredecessorTaskId, predecessorId);
        query.eq(TaskDependency::getSuccessorTaskId, successorId);
        int deleted = taskDependencyMapper.delete(query);
        if (deleted == 0) {
            throw new BusinessException("Dependency not found");
        }
    }

    @Override
    public List<TaskResponse> getDependencies(Long taskId) {
        // Get all dependencies where this task is the successor (predecessor tasks)
        LambdaQueryWrapper<TaskDependency> query = new LambdaQueryWrapper<>();
        query.eq(TaskDependency::getSuccessorTaskId, taskId);
        List<TaskDependency> dependencies = taskDependencyMapper.selectList(query);

        if (dependencies.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> predecessorIds = dependencies.stream()
                .map(TaskDependency::getPredecessorTaskId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Task> taskQuery = new LambdaQueryWrapper<>();
        taskQuery.in(Task::getId, predecessorIds);
        List<Task> tasks = taskMapper.selectList(taskQuery);

        return tasks.stream().map(this::buildTaskResponse).collect(Collectors.toList());
    }

    private TaskResponse buildTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setWorkspaceId(task.getWorkspaceId());
        response.setBoardColumnId(task.getBoardColumnId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setPriority(task.getPriority());
        response.setStatus(task.getStatus());
        response.setStartDate(task.getStartDate());
        response.setDueDate(task.getDueDate());
        response.setProgress(task.getProgress());
        response.setAssigneeId(task.getAssigneeId());
        response.setCreatorId(task.getCreatorId());
        response.setParentTaskId(task.getParentTaskId());
        response.setPosition(task.getPosition());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        // Fetch assignee
        if (task.getAssigneeId() != null) {
            User assignee = userMapper.selectById(task.getAssigneeId());
            if (assignee != null) {
                response.setAssignee(new UserResponse(
                        assignee.getId(), assignee.getUsername(), assignee.getEmail(), assignee.getAvatarUrl()));
            }
        }

        // Fetch creator
        if (task.getCreatorId() != null) {
            User creator = userMapper.selectById(task.getCreatorId());
            if (creator != null) {
                response.setCreator(new UserResponse(
                        creator.getId(), creator.getUsername(), creator.getEmail(), creator.getAvatarUrl()));
            }
        }

        // Fetch subtasks
        LambdaQueryWrapper<Subtask> subtaskQuery = new LambdaQueryWrapper<>();
        subtaskQuery.eq(Subtask::getTaskId, task.getId());
        subtaskQuery.orderByAsc(Subtask::getPosition);
        List<Subtask> subtasks = subtaskMapper.selectList(subtaskQuery);
        List<SubtaskResponse> subtaskResponses = subtasks.stream()
                .map(s -> new SubtaskResponse(s.getId(), s.getTaskId(), s.getTitle(), s.getIsCompleted(), s.getPosition()))
                .collect(Collectors.toList());
        response.setSubtasks(subtaskResponses);

        // Fetch comment count
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        commentQuery.eq(Comment::getTaskId, task.getId());
        Long commentCount = commentMapper.selectCount(commentQuery);
        response.setCommentCount(commentCount.intValue());

        return response;
    }
}
