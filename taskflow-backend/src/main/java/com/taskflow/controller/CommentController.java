package com.taskflow.controller;

import com.taskflow.dto.request.CommentCreateRequest;
import com.taskflow.dto.request.CommentUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.CommentResponse;
import com.taskflow.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/v1/tasks/{taskId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long taskId,
            @Validated @RequestBody CommentCreateRequest request) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        CommentResponse response = commentService.createComment(taskId, userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/tasks/{taskId}/comments")
    public ApiResponse<List<CommentResponse>> listComments(@PathVariable Long taskId) {
        List<CommentResponse> response = commentService.listComments(taskId);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/comments/{id}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long id,
            @Validated @RequestBody CommentUpdateRequest request) {
        CommentResponse response = commentService.updateComment(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ApiResponse.success();
    }
}
