package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.ReviewsApi;
import com.bassstrings.database.api.model.Review;
import com.bassstrings.database.api.model.ReviewPage;
import com.bassstrings.database.api.model.ReviewRequest;
import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.domain.UserAccount;
import com.bassstrings.database.mapper.ReviewMapper;
import com.bassstrings.database.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.bassstrings.database.security.CurrentUserSupport.currentUser;

/** Implements the {@link ReviewsApi} interface generated from openapi/bassstrings-api.yaml. */
@RestController
@RequiredArgsConstructor
public class ReviewsApiController implements ReviewsApi {

    private final ReviewService service;
    private final ReviewMapper mapper;

    @Override
    public ResponseEntity<ReviewPage> listReviewsForBassString(String bassStringId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(mapper.toPage(service.listForBassString(bassStringId, pageable)));
    }

    @Override
    public ResponseEntity<Review> createReview(String bassStringId, ReviewRequest reviewRequest) {
        UserAccount author = currentUser();
        StringReview created = service.create(bassStringId, mapper.toEntity(reviewRequest), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Override
    public ResponseEntity<Review> getReview(String reviewId) {
        return ResponseEntity.ok(mapper.toDto(service.get(reviewId)));
    }

    @Override
    public ResponseEntity<Review> updateReview(String reviewId, ReviewRequest reviewRequest) {
        StringReview updated = service.update(reviewId, mapper.toEntity(reviewRequest), currentUser());
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteReview(String reviewId) {
        service.delete(reviewId, currentUser());
        return ResponseEntity.noContent().build();
    }
}
