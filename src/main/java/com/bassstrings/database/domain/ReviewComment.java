package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.CommentStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A comment left on a {@link StringReview}. */
@Document(collection = "comments")
@Getter
@Setter
public class ReviewComment {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    private String authorId;

    private String authorDisplayName;

    private String body;

    private CommentStatus status = CommentStatus.PUBLISHED;

    @CreatedDate
    private Instant createdAt;
}
