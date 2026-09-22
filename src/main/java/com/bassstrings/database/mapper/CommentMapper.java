package com.bassstrings.database.mapper;

import com.bassstrings.database.api.model.Comment;
import com.bassstrings.database.api.model.CommentPage;
import com.bassstrings.database.api.model.CommentRequest;
import com.bassstrings.database.domain.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/** Converts between the generated {@code api.model} DTOs and the persisted {@link ReviewComment}. */
@Component
public class CommentMapper {

    public Comment toDto(ReviewComment entity) {
        return new Comment()
                .id(entity.getId())
                .reviewId(entity.getReviewId())
                .authorId(entity.getAuthorId())
                .authorDisplayName(entity.getAuthorDisplayName())
                .body(entity.getBody())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().atOffset(ZoneOffset.UTC));
    }

    public ReviewComment toEntity(CommentRequest request) {
        ReviewComment entity = new ReviewComment();
        entity.setBody(request.getBody());
        return entity;
    }

    public CommentPage toPage(Page<ReviewComment> page) {
        return new CommentPage()
                .content(page.getContent().stream().map(this::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}
