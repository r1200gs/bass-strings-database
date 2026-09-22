package com.bassstrings.database.repository;

import com.bassstrings.database.api.model.ReviewStatus;
import com.bassstrings.database.domain.StringReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<StringReview, String> {

    Page<StringReview> findByBassStringIdAndStatus(String bassStringId, ReviewStatus status, Pageable pageable);

    List<StringReview> findByBassStringIdAndStatus(String bassStringId, ReviewStatus status);

    long countByBassStringIdAndStatus(String bassStringId, ReviewStatus status);

    /** Powers the "reviews that mention this bass" cross-reference on the model detail page. */
    List<StringReview> findByBassModelIdAndStatus(String bassModelId, ReviewStatus status);
}
