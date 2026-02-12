package com.taskflow.service;

import com.taskflow.dto.request.CommentCreateRequest;
import com.taskflow.dto.request.CommentUpdateRequest;
import com.taskflow.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long taskId, Long userId, CommentCreateRequest request);

    CommentResponse updateComment(Long id, CommentUpdateRequest request);

    void deleteComment(Long id);

    List<CommentResponse> listComments(Long taskId);
}
