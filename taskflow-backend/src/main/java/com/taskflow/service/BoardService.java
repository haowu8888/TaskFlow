package com.taskflow.service;

import com.taskflow.dto.request.BoardColumnCreateRequest;
import com.taskflow.dto.request.BoardColumnReorderRequest;
import com.taskflow.dto.request.BoardColumnUpdateRequest;
import com.taskflow.dto.request.BoardCreateRequest;
import com.taskflow.dto.response.BoardColumnResponse;
import com.taskflow.dto.response.BoardResponse;

import java.util.List;

public interface BoardService {

    BoardResponse createBoard(Long workspaceId, BoardCreateRequest request, Long createdBy);

    BoardResponse getBoard(Long id);

    List<BoardResponse> listBoards(Long workspaceId);

    BoardResponse updateBoard(Long id, BoardCreateRequest request);

    void deleteBoard(Long id);

    BoardColumnResponse addColumn(Long boardId, BoardColumnCreateRequest request);

    BoardColumnResponse updateColumn(Long id, BoardColumnUpdateRequest request);

    void deleteColumn(Long id);

    void reorderColumns(Long boardId, BoardColumnReorderRequest request);
}
