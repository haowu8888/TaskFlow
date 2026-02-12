package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.request.LabelCreateRequest;
import com.taskflow.dto.request.LabelUpdateRequest;
import com.taskflow.dto.response.LabelResponse;
import com.taskflow.entity.Label;
import com.taskflow.entity.TaskLabel;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.LabelMapper;
import com.taskflow.mapper.TaskLabelMapper;
import com.taskflow.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelMapper labelMapper;
    private final TaskLabelMapper taskLabelMapper;

    @Override
    @Transactional
    public LabelResponse createLabel(Long workspaceId, Long userId, LabelCreateRequest request) {
        Label label = new Label();
        label.setWorkspaceId(workspaceId);
        label.setName(request.getName());
        label.setColor(request.getColor());
        label.setCreatedBy(userId);
        label.setCreatedAt(LocalDateTime.now());
        label.setUpdatedAt(LocalDateTime.now());
        label.setDeleted(0);
        labelMapper.insert(label);
        return buildResponse(label);
    }

    @Override
    @Transactional
    public LabelResponse updateLabel(Long id, LabelUpdateRequest request) {
        Label label = labelMapper.selectById(id);
        if (label == null) {
            throw new BusinessException("Label not found");
        }
        if (StringUtils.hasText(request.getName())) {
            label.setName(request.getName());
        }
        if (StringUtils.hasText(request.getColor())) {
            label.setColor(request.getColor());
        }
        label.setUpdatedAt(LocalDateTime.now());
        labelMapper.updateById(label);
        return buildResponse(label);
    }

    @Override
    public void deleteLabel(Long id) {
        Label label = labelMapper.selectById(id);
        if (label == null) {
            throw new BusinessException("Label not found");
        }
        // Remove all task-label associations
        taskLabelMapper.delete(new LambdaQueryWrapper<TaskLabel>().eq(TaskLabel::getLabelId, id));
        labelMapper.deleteById(id);
    }

    @Override
    public List<LabelResponse> listLabels(Long workspaceId) {
        LambdaQueryWrapper<Label> query = new LambdaQueryWrapper<>();
        query.eq(Label::getWorkspaceId, workspaceId);
        query.orderByAsc(Label::getName);
        return labelMapper.selectList(query).stream().map(this::buildResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addLabelToTask(Long taskId, Long labelId) {
        // Check if association already exists
        LambdaQueryWrapper<TaskLabel> query = new LambdaQueryWrapper<>();
        query.eq(TaskLabel::getTaskId, taskId).eq(TaskLabel::getLabelId, labelId);
        if (taskLabelMapper.selectCount(query) > 0) {
            return; // Already associated
        }
        TaskLabel taskLabel = new TaskLabel();
        taskLabel.setTaskId(taskId);
        taskLabel.setLabelId(labelId);
        taskLabelMapper.insert(taskLabel);
    }

    @Override
    @Transactional
    public void removeLabelFromTask(Long taskId, Long labelId) {
        LambdaQueryWrapper<TaskLabel> query = new LambdaQueryWrapper<>();
        query.eq(TaskLabel::getTaskId, taskId).eq(TaskLabel::getLabelId, labelId);
        taskLabelMapper.delete(query);
    }

    @Override
    public List<LabelResponse> getTaskLabels(Long taskId) {
        LambdaQueryWrapper<TaskLabel> query = new LambdaQueryWrapper<>();
        query.eq(TaskLabel::getTaskId, taskId);
        List<TaskLabel> taskLabels = taskLabelMapper.selectList(query);
        return taskLabels.stream()
                .map(tl -> {
                    Label label = labelMapper.selectById(tl.getLabelId());
                    return label != null ? buildResponse(label) : null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    private LabelResponse buildResponse(Label label) {
        LabelResponse response = new LabelResponse();
        response.setId(label.getId());
        response.setWorkspaceId(label.getWorkspaceId());
        response.setName(label.getName());
        response.setColor(label.getColor());
        response.setCreatedAt(label.getCreatedAt());
        return response;
    }
}
