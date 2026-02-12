package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.request.TaskMoveRequest;
import com.taskflow.dto.request.TaskStatusUpdateRequest;
import com.taskflow.dto.request.TaskUpdateRequest;
import com.taskflow.dto.response.SubtaskResponse;
import com.taskflow.dto.response.TaskListResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Subtask;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.CommentMapper;
import com.taskflow.mapper.SubtaskMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final SubtaskMapper subtaskMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public TaskResponse createTask(Long workspaceId, TaskCreateRequest request, Long creatorId) {
        Task task = new Task();
        task.setWorkspaceId(workspaceId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        task.setStatus(request.getStatus() != null ? request.getStatus() : "TODO");
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setAssigneeId(request.getAssigneeId());
        task.setCreatorId(creatorId);
        task.setParentTaskId(request.getParentTaskId());
        task.setBoardColumnId(request.getBoardColumnId());
        task.setProgress(0);
        task.setPosition(0);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(0);
        taskMapper.insert(task);

        return buildTaskResponse(task);
    }

    @Override
    public TaskResponse getTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("Task not found");
        }
        return buildTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("Task not found");
        }

        if (StringUtils.hasText(request.getTitle())) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (StringUtils.hasText(request.getPriority())) {
            task.setPriority(request.getPriority());
        }
        if (StringUtils.hasText(request.getStatus())) {
            task.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            task.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getParentTaskId() != null) {
            task.setParentTaskId(request.getParentTaskId());
        }
        if (request.getBoardColumnId() != null) {
            task.setBoardColumnId(request.getBoardColumnId());
        }
        if (request.getProgress() != null) {
            task.setProgress(request.getProgress());
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);

        return buildTaskResponse(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("Task not found");
        }
        taskMapper.deleteById(id);
    }

    @Override
    public TaskListResponse listTasks(Long workspaceId, String status, String priority, Long assigneeId,
                                       String keyword, int page, int size, String sortBy, String sortDir) {
        Page<Task> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getWorkspaceId, workspaceId);

        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Task::getStatus, status);
        }
        if (StringUtils.hasText(priority)) {
            queryWrapper.eq(Task::getPriority, priority);
        }
        if (assigneeId != null) {
            queryWrapper.eq(Task::getAssigneeId, assigneeId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Task::getTitle, keyword);
        }

        // Sorting
        boolean isAsc = "asc".equalsIgnoreCase(sortDir);
        switch (sortBy) {
            case "title":
                queryWrapper.orderBy(true, isAsc, Task::getTitle);
                break;
            case "priority":
                queryWrapper.orderBy(true, isAsc, Task::getPriority);
                break;
            case "status":
                queryWrapper.orderBy(true, isAsc, Task::getStatus);
                break;
            case "dueDate":
                queryWrapper.orderBy(true, isAsc, Task::getDueDate);
                break;
            case "updatedAt":
                queryWrapper.orderBy(true, isAsc, Task::getUpdatedAt);
                break;
            default:
                queryWrapper.orderBy(true, isAsc, Task::getCreatedAt);
                break;
        }

        Page<Task> resultPage = taskMapper.selectPage(pageObj, queryWrapper);

        List<TaskResponse> records = resultPage.getRecords().stream()
                .map(this::buildTaskResponse)
                .collect(Collectors.toList());

        TaskListResponse response = new TaskListResponse();
        response.setRecords(records);
        response.setTotal(resultPage.getTotal());
        response.setSize(resultPage.getSize());
        response.setCurrent(resultPage.getCurrent());
        response.setPages(resultPage.getPages());
        return response;
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("Task not found");
        }
        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return buildTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long id, TaskMoveRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("Task not found");
        }
        task.setBoardColumnId(request.getBoardColumnId());
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
        return buildTaskResponse(task);
    }

    @Override
    public List<TaskResponse> getCalendarTasks(Long workspaceId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getWorkspaceId, workspaceId);
        if (start != null) {
            queryWrapper.ge(Task::getDueDate, start);
        }
        if (end != null) {
            queryWrapper.le(Task::getDueDate, end);
        }
        queryWrapper.orderByAsc(Task::getDueDate);

        List<Task> tasks = taskMapper.selectList(queryWrapper);
        return tasks.stream().map(this::buildTaskResponse).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getGanttTasks(Long workspaceId) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getWorkspaceId, workspaceId);
        queryWrapper.orderByAsc(Task::getStartDate);

        List<Task> tasks = taskMapper.selectList(queryWrapper);
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
