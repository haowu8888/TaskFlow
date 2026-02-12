package com.taskflow.service;

import com.taskflow.dto.request.SubtaskCreateRequest;
import com.taskflow.dto.request.SubtaskReorderRequest;
import com.taskflow.dto.request.SubtaskUpdateRequest;
import com.taskflow.dto.response.SubtaskResponse;

import java.util.List;

public interface SubtaskService {

    SubtaskResponse createSubtask(Long taskId, SubtaskCreateRequest request);

    SubtaskResponse updateSubtask(Long id, SubtaskUpdateRequest request);

    void deleteSubtask(Long id);

    SubtaskResponse toggleSubtask(Long id);

    void reorderSubtasks(SubtaskReorderRequest request);

    List<SubtaskResponse> listSubtasks(Long taskId);
}
