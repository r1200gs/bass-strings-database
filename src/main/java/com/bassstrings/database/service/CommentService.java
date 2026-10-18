package com.bassstrings.database.service;

import com.bassstrings.database.api.model.CommentStatus;
import com.bassstrings.database.api.model.Role;
import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.UserAccount;
import com.bassstrings.database.exception.ResourceNotFoundException;
import com.bassstrings.database.repository.CommentRepository;
import com.bassstrings.database.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

    public Page<ReviewComment> listForReview(String reviewId, Pageable pageable) {
        return commentRepository.findByReviewIdAndStatus(reviewId, CommentStatus.PUBLISHED, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    public ReviewComment create(String reviewId, ReviewComment comment, UserAccount author) {
        if (!reviewRepository.existsById(reviewId)) {
            throw ResourceNotFoundException.forId("Review", reviewId);
        }
        comment.setReviewId(reviewId);
        comment.setAuthorId(author.getId());
        comment.setAuthorDisplayName(author.getDisplayName());
        comment.setStatus(CommentStatus.PUBLISHED);
        return commentRepository.save(comment);
    }

    public void delete(String commentId, UserAccount currentUser) {
        ReviewComment existing = commentRepository.findById(commentId)
                .orElseThrow(() -> ResourceNotFoundException.forId("Comment", commentId));

        boolean isAuthor = existing.getAuthorId().equals(currentUser.getId());
        boolean canModerate = currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.MONITOR);
        if (!isAuthor && !canModerate) {
            throw new AccessDeniedException("Only the comment's author, a monitor, or an admin can remove it");
        }

        commentRepository.deleteById(commentId);
    }
}
