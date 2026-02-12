package com.taskflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskflow.dto.request.CommentCreateRequest;
import com.taskflow.dto.request.CommentUpdateRequest;
import com.taskflow.dto.response.CommentResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.User;
import com.taskflow.exception.BusinessException;
import com.taskflow.mapper.CommentMapper;
import com.taskflow.mapper.UserMapper;
import com.taskflow.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CommentResponse createComment(Long taskId, Long userId, CommentCreateRequest request) {
        Comment comment = new Comment();
        comment.setTaskId(taskId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setDeleted(0);
        commentMapper.insert(comment);

        return buildCommentResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long id, CommentUpdateRequest request) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }

        if (StringUtils.hasText(request.getContent())) {
            comment.setContent(request.getContent());
        }
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);

        return buildCommentResponse(comment);
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public List<CommentResponse> listComments(Long taskId) {
        LambdaQueryWrapper<Comment> query = new LambdaQueryWrapper<>();
        query.eq(Comment::getTaskId, taskId);
        query.orderByAsc(Comment::getCreatedAt);
        List<Comment> comments = commentMapper.selectList(query);
        return comments.stream().map(this::buildCommentResponse).collect(Collectors.toList());
    }

    private CommentResponse buildCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setTaskId(comment.getTaskId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        // Fetch user
        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                response.setUser(new UserResponse(
                        user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl()));
            }
        }

        return response;
    }
}
