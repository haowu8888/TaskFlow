package com.taskflow.service;

import com.taskflow.dto.request.LabelCreateRequest;
import com.taskflow.dto.request.LabelUpdateRequest;
import com.taskflow.dto.response.LabelResponse;

import java.util.List;

public interface LabelService {

    LabelResponse createLabel(Long workspaceId, Long userId, LabelCreateRequest request);

    LabelResponse updateLabel(Long id, LabelUpdateRequest request);

    void deleteLabel(Long id);

    List<LabelResponse> listLabels(Long workspaceId);

    void addLabelToTask(Long taskId, Long labelId);

    void removeLabelFromTask(Long taskId, Long labelId);

    List<LabelResponse> getTaskLabels(Long taskId);
}
