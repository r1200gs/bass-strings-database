package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.CommentsApi;
import com.bassstrings.database.api.model.Comment;
import com.bassstrings.database.api.model.CommentPage;
import com.bassstrings.database.api.model.CommentRequest;
import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.UserAccount;
import com.bassstrings.database.mapper.CommentMapper;
import com.bassstrings.database.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.bassstrings.database.security.CurrentUserSupport.currentUser;

/** Implements the {@link CommentsApi} interface generated from openapi/bassstrings-api.yaml. */
@RestController
@RequiredArgsConstructor
public class CommentsApiController implements CommentsApi {

    private final CommentService service;
    private final CommentMapper mapper;

    @Override
    public ResponseEntity<CommentPage> listCommentsForReview(String reviewId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(mapper.toPage(service.listForReview(reviewId, pageable)));
    }

    @Override
    public ResponseEntity<Comment> createComment(String reviewId, CommentRequest commentRequest) {
        UserAccount author = currentUser();
        ReviewComment created = service.create(reviewId, mapper.toEntity(commentRequest), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Override
    public ResponseEntity<Void> deleteComment(String commentId) {
        service.delete(commentId, currentUser());
        return ResponseEntity.noContent().build();
    }
}
