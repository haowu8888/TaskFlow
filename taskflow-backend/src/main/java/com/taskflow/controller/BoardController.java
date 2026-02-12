package com.taskflow.controller;

import com.taskflow.dto.request.BoardColumnCreateRequest;
import com.taskflow.dto.request.BoardColumnReorderRequest;
import com.taskflow.dto.request.BoardColumnUpdateRequest;
import com.taskflow.dto.request.BoardCreateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.BoardColumnResponse;
import com.taskflow.dto.response.BoardResponse;
import com.taskflow.service.BoardService;
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
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/api/v1/workspaces/{workspaceId}/boards")
    public ApiResponse<BoardResponse> createBoard(
            @PathVariable Long workspaceId,
            @Validated @RequestBody BoardCreateRequest request) {
        Long createdBy = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        BoardResponse response = boardService.createBoard(workspaceId, request, createdBy);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/workspaces/{workspaceId}/boards")
    public ApiResponse<List<BoardResponse>> listBoards(@PathVariable Long workspaceId) {
        List<BoardResponse> response = boardService.listBoards(workspaceId);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/boards/{id}")
    public ApiResponse<BoardResponse> getBoard(@PathVariable Long id) {
        BoardResponse response = boardService.getBoard(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/boards/{id}")
    public ApiResponse<BoardResponse> updateBoard(
            @PathVariable Long id,
            @Validated @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.updateBoard(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/boards/{id}")
    public ApiResponse<Void> deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
        return ApiResponse.success();
    }

    @PostMapping("/api/v1/boards/{id}/columns")
    public ApiResponse<BoardColumnResponse> addColumn(
            @PathVariable Long id,
            @Validated @RequestBody BoardColumnCreateRequest request) {
        BoardColumnResponse response = boardService.addColumn(id, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/api/v1/columns/{id}")
    public ApiResponse<BoardColumnResponse> updateColumn(
            @PathVariable Long id,
            @RequestBody BoardColumnUpdateRequest request) {
        BoardColumnResponse response = boardService.updateColumn(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/api/v1/columns/{id}")
    public ApiResponse<Void> deleteColumn(@PathVariable Long id) {
        boardService.deleteColumn(id);
        return ApiResponse.success();
    }

    @PutMapping("/api/v1/boards/{id}/columns/reorder")
    public ApiResponse<Void> reorderColumns(
            @PathVariable Long id,
            @Validated @RequestBody BoardColumnReorderRequest request) {
        boardService.reorderColumns(id, request);
        return ApiResponse.success();
    }
}
