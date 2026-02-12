package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.taskflow.dto.request.BoardColumnCreateRequest;
import com.taskflow.dto.request.BoardColumnReorderRequest;
import com.taskflow.dto.request.BoardColumnUpdateRequest;
import com.taskflow.dto.request.BoardCreateRequest;
import com.taskflow.dto.response.BoardColumnResponse;
import com.taskflow.dto.response.BoardResponse;
import com.taskflow.dto.response.SubtaskResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Board;
import com.taskflow.entity.BoardColumn;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Subtask;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.BoardColumnMapper;
import com.taskflow.mapper.BoardMapper;
import com.taskflow.mapper.CommentMapper;
import com.taskflow.mapper.SubtaskMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardMapper boardMapper;
    private final BoardColumnMapper boardColumnMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final SubtaskMapper subtaskMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public BoardResponse createBoard(Long workspaceId, BoardCreateRequest request, Long createdBy) {
        Board board = new Board();
        board.setWorkspaceId(workspaceId);
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setCreatedBy(createdBy);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        board.setDeleted(0);
        boardMapper.insert(board);

        // Create 3 default columns: To Do, In Progress, Done
        String[][] defaultColumns = {
                {"To Do", "#e2e8f0"},
                {"In Progress", "#fbbf24"},
                {"Done", "#34d399"}
        };
        for (int i = 0; i < defaultColumns.length; i++) {
            BoardColumn column = new BoardColumn();
            column.setBoardId(board.getId());
            column.setName(defaultColumns[i][0]);
            column.setColor(defaultColumns[i][1]);
            column.setPosition(i);
            column.setCreatedAt(LocalDateTime.now());
            column.setUpdatedAt(LocalDateTime.now());
            boardColumnMapper.insert(column);
        }

        return getBoard(board.getId());
    }

    @Override
    public BoardResponse getBoard(Long id) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("Board not found");
        }

        // Get columns with their tasks
        LambdaQueryWrapper<BoardColumn> columnQuery = new LambdaQueryWrapper<>();
        columnQuery.eq(BoardColumn::getBoardId, board.getId());
        columnQuery.orderByAsc(BoardColumn::getPosition);
        List<BoardColumn> columns = boardColumnMapper.selectList(columnQuery);

        List<BoardColumnResponse> columnResponses = columns.stream()
                .map(col -> {
                    BoardColumnResponse colResponse = new BoardColumnResponse();
                    colResponse.setId(col.getId());
                    colResponse.setBoardId(col.getBoardId());
                    colResponse.setName(col.getName());
                    colResponse.setColor(col.getColor());
                    colResponse.setPosition(col.getPosition());
                    colResponse.setWipLimit(col.getWipLimit());

                    // Fetch tasks for this column
                    LambdaQueryWrapper<Task> taskQuery = new LambdaQueryWrapper<>();
                    taskQuery.eq(Task::getBoardColumnId, col.getId());
                    taskQuery.orderByAsc(Task::getPosition);
                    List<Task> tasks = taskMapper.selectList(taskQuery);
                    List<TaskResponse> taskResponses = tasks.stream()
                            .map(this::buildTaskResponse)
                            .collect(Collectors.toList());
                    colResponse.setTasks(taskResponses);

                    return colResponse;
                })
                .collect(Collectors.toList());

        BoardResponse response = new BoardResponse();
        response.setId(board.getId());
        response.setWorkspaceId(board.getWorkspaceId());
        response.setName(board.getName());
        response.setDescription(board.getDescription());
        response.setColumns(columnResponses);
        return response;
    }

    @Override
    public List<BoardResponse> listBoards(Long workspaceId) {
        LambdaQueryWrapper<Board> query = new LambdaQueryWrapper<>();
        query.eq(Board::getWorkspaceId, workspaceId);
        query.orderByDesc(Board::getCreatedAt);
        List<Board> boards = boardMapper.selectList(query);

        return boards.stream().map(board -> {
            BoardResponse response = new BoardResponse();
            response.setId(board.getId());
            response.setWorkspaceId(board.getWorkspaceId());
            response.setName(board.getName());
            response.setDescription(board.getDescription());

            // Get columns without tasks for list view
            LambdaQueryWrapper<BoardColumn> columnQuery = new LambdaQueryWrapper<>();
            columnQuery.eq(BoardColumn::getBoardId, board.getId());
            columnQuery.orderByAsc(BoardColumn::getPosition);
            List<BoardColumn> columns = boardColumnMapper.selectList(columnQuery);
            List<BoardColumnResponse> columnResponses = columns.stream()
                    .map(col -> {
                        BoardColumnResponse colResp = new BoardColumnResponse();
                        colResp.setId(col.getId());
                        colResp.setBoardId(col.getBoardId());
                        colResp.setName(col.getName());
                        colResp.setColor(col.getColor());
                        colResp.setPosition(col.getPosition());
                        colResp.setWipLimit(col.getWipLimit());
                        colResp.setTasks(new ArrayList<>());
                        return colResp;
                    })
                    .collect(Collectors.toList());
            response.setColumns(columnResponses);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(Long id, BoardCreateRequest request) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("Board not found");
        }

        if (StringUtils.hasText(request.getName())) {
            board.setName(request.getName());
        }
        if (request.getDescription() != null) {
            board.setDescription(request.getDescription());
        }
        board.setUpdatedAt(LocalDateTime.now());
        boardMapper.updateById(board);

        return getBoard(board.getId());
    }

    @Override
    public void deleteBoard(Long id) {
        Board board = boardMapper.selectById(id);
        if (board == null) {
            throw new BusinessException("Board not found");
        }
        boardMapper.deleteById(id);
    }

    @Override
    @Transactional
    public BoardColumnResponse addColumn(Long boardId, BoardColumnCreateRequest request) {
        Board board = boardMapper.selectById(boardId);
        if (board == null) {
            throw new BusinessException("Board not found");
        }

        // Get max position
        LambdaQueryWrapper<BoardColumn> query = new LambdaQueryWrapper<>();
        query.eq(BoardColumn::getBoardId, boardId);
        query.orderByDesc(BoardColumn::getPosition);
        query.last("LIMIT 1");
        BoardColumn lastColumn = boardColumnMapper.selectOne(query);
        int nextPosition = (lastColumn != null && lastColumn.getPosition() != null) ? lastColumn.getPosition() + 1 : 0;

        BoardColumn column = new BoardColumn();
        column.setBoardId(boardId);
        column.setName(request.getName());
        column.setColor(request.getColor());
        column.setWipLimit(request.getWipLimit());
        column.setPosition(nextPosition);
        column.setCreatedAt(LocalDateTime.now());
        column.setUpdatedAt(LocalDateTime.now());
        boardColumnMapper.insert(column);

        BoardColumnResponse response = new BoardColumnResponse();
        response.setId(column.getId());
        response.setBoardId(column.getBoardId());
        response.setName(column.getName());
        response.setColor(column.getColor());
        response.setPosition(column.getPosition());
        response.setWipLimit(column.getWipLimit());
        response.setTasks(new ArrayList<>());
        return response;
    }

    @Override
    @Transactional
    public BoardColumnResponse updateColumn(Long id, BoardColumnUpdateRequest request) {
        BoardColumn column = boardColumnMapper.selectById(id);
        if (column == null) {
            throw new BusinessException("Board column not found");
        }

        if (StringUtils.hasText(request.getName())) {
            column.setName(request.getName());
        }
        if (request.getColor() != null) {
            column.setColor(request.getColor());
        }
        if (request.getWipLimit() != null) {
            column.setWipLimit(request.getWipLimit());
        }
        column.setUpdatedAt(LocalDateTime.now());
        boardColumnMapper.updateById(column);

        BoardColumnResponse response = new BoardColumnResponse();
        response.setId(column.getId());
        response.setBoardId(column.getBoardId());
        response.setName(column.getName());
        response.setColor(column.getColor());
        response.setPosition(column.getPosition());
        response.setWipLimit(column.getWipLimit());
        response.setTasks(new ArrayList<>());
        return response;
    }

    @Override
    public void deleteColumn(Long id) {
        BoardColumn column = boardColumnMapper.selectById(id);
        if (column == null) {
            throw new BusinessException("Board column not found");
        }
        boardColumnMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void reorderColumns(Long boardId, BoardColumnReorderRequest request) {
        for (BoardColumnReorderRequest.ColumnOrderItem item : request.getItems()) {
            LambdaUpdateWrapper<BoardColumn> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(BoardColumn::getId, item.getId());
            updateWrapper.eq(BoardColumn::getBoardId, boardId);
            updateWrapper.set(BoardColumn::getPosition, item.getPosition());
            updateWrapper.set(BoardColumn::getUpdatedAt, LocalDateTime.now());
            boardColumnMapper.update(null, updateWrapper);
        }
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
