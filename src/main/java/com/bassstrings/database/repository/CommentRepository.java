package com.bassstrings.database.repository;

import com.bassstrings.database.api.model.CommentStatus;
import com.bassstrings.database.domain.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<ReviewComment, String> {

    Page<ReviewComment> findByReviewIdAndStatus(String reviewId, CommentStatus status, Pageable pageable);
}
