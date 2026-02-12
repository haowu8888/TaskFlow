package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.taskflow.dto.request.SubtaskCreateRequest;
import com.taskflow.dto.request.SubtaskReorderRequest;
import com.taskflow.dto.request.SubtaskUpdateRequest;
import com.taskflow.dto.response.SubtaskResponse;
import com.taskflow.entity.Subtask;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.SubtaskMapper;
import com.taskflow.service.SubtaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubtaskServiceImpl implements SubtaskService {

    private final SubtaskMapper subtaskMapper;

    @Override
    @Transactional
    public SubtaskResponse createSubtask(Long taskId, SubtaskCreateRequest request) {
        // Get max position for this task
        LambdaQueryWrapper<Subtask> query = new LambdaQueryWrapper<>();
        query.eq(Subtask::getTaskId, taskId);
        query.orderByDesc(Subtask::getPosition);
        query.last("LIMIT 1");
        Subtask lastSubtask = subtaskMapper.selectOne(query);
        int nextPosition = (lastSubtask != null && lastSubtask.getPosition() != null) ? lastSubtask.getPosition() + 1 : 0;

        Subtask subtask = new Subtask();
        subtask.setTaskId(taskId);
        subtask.setTitle(request.getTitle());
        subtask.setIsCompleted(0);
        subtask.setPosition(nextPosition);
        subtask.setCreatedAt(LocalDateTime.now());
        subtask.setUpdatedAt(LocalDateTime.now());
        subtaskMapper.insert(subtask);

        return toSubtaskResponse(subtask);
    }

    @Override
    @Transactional
    public SubtaskResponse updateSubtask(Long id, SubtaskUpdateRequest request) {
        Subtask subtask = subtaskMapper.selectById(id);
        if (subtask == null) {
            throw new BusinessException("Subtask not found");
        }

        if (StringUtils.hasText(request.getTitle())) {
            subtask.setTitle(request.getTitle());
        }
        if (request.getIsCompleted() != null) {
            subtask.setIsCompleted(request.getIsCompleted());
        }
        subtask.setUpdatedAt(LocalDateTime.now());
        subtaskMapper.updateById(subtask);

        return toSubtaskResponse(subtask);
    }

    @Override
    public void deleteSubtask(Long id) {
        Subtask subtask = subtaskMapper.selectById(id);
        if (subtask == null) {
            throw new BusinessException("Subtask not found");
        }
        subtaskMapper.deleteById(id);
    }

    @Override
    @Transactional
    public SubtaskResponse toggleSubtask(Long id) {
        Subtask subtask = subtaskMapper.selectById(id);
        if (subtask == null) {
            throw new BusinessException("Subtask not found");
        }

        // Flip isCompleted
        subtask.setIsCompleted(subtask.getIsCompleted() == 1 ? 0 : 1);
        subtask.setUpdatedAt(LocalDateTime.now());
        subtaskMapper.updateById(subtask);

        return toSubtaskResponse(subtask);
    }

    @Override
    @Transactional
    public void reorderSubtasks(SubtaskReorderRequest request) {
        for (SubtaskReorderRequest.SubtaskOrderItem item : request.getItems()) {
            LambdaUpdateWrapper<Subtask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Subtask::getId, item.getId());
            updateWrapper.set(Subtask::getPosition, item.getPosition());
            updateWrapper.set(Subtask::getUpdatedAt, LocalDateTime.now());
            subtaskMapper.update(null, updateWrapper);
        }
    }

    @Override
    public List<SubtaskResponse> listSubtasks(Long taskId) {
        LambdaQueryWrapper<Subtask> query = new LambdaQueryWrapper<>();
        query.eq(Subtask::getTaskId, taskId);
        query.orderByAsc(Subtask::getPosition);
        List<Subtask> subtasks = subtaskMapper.selectList(query);
        return subtasks.stream().map(this::toSubtaskResponse).collect(Collectors.toList());
    }

    private SubtaskResponse toSubtaskResponse(Subtask subtask) {
        return new SubtaskResponse(
                subtask.getId(),
                subtask.getTaskId(),
                subtask.getTitle(),
                subtask.getIsCompleted(),
                subtask.getPosition()
        );
    }
}
