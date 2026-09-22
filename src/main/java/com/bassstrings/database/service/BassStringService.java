package com.bassstrings.database.service;

import com.bassstrings.database.domain.BassStringProduct;
import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.exception.ResourceNotFoundException;
import com.bassstrings.database.repository.BassStringProductRepository;
import com.bassstrings.database.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class BassStringService {

    private final BassStringProductRepository repository;
    private final ReviewRepository reviewRepository;
    private final MongoTemplate mongoTemplate;

    public BassStringService(BassStringProductRepository repository,
                              ReviewRepository reviewRepository,
                              MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.reviewRepository = reviewRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<BassStringProduct> search(BassStringSearchCriteria criteria, Pageable pageable) {
        List<Criteria> conditions = new ArrayList<>();
        if (StringUtils.hasText(criteria.brand())) {
            conditions.add(Criteria.where("brand").regex(".*" + Pattern.quote(criteria.brand()) + ".*", "i"));
        }
        if (criteria.material() != null) {
            conditions.add(Criteria.where("material").is(criteria.material()));
        }
        if (criteria.construction() != null) {
            conditions.add(Criteria.where("construction").is(criteria.construction()));
        }
        if (criteria.numStrings() != null) {
            conditions.add(Criteria.where("numStrings").is(criteria.numStrings()));
        }
        if (criteria.playingStyle() != null) {
            conditions.add(Criteria.where("suitablePlayingStyles").is(criteria.playingStyle()));
        }
        if (criteria.genre() != null) {
            conditions.add(Criteria.where("taggedGenres").is(criteria.genre()));
        }
        if (StringUtils.hasText(criteria.freeTextQuery())) {
            String pattern = ".*" + Pattern.quote(criteria.freeTextQuery()) + ".*";
            conditions.add(new Criteria().orOperator(
                    Criteria.where("brand").regex(pattern, "i"),
                    Criteria.where("productLine").regex(pattern, "i"),
                    Criteria.where("description").regex(pattern, "i")
            ));
        }

        Query query = conditions.isEmpty()
                ? new Query()
                : new Query(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, BassStringProduct.class);
        query.with(pageable);
        List<BassStringProduct> content = mongoTemplate.find(query, BassStringProduct.class);
        return new PageImpl<>(content, pageable, total);
    }

    public BassStringProduct get(String id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Bass string", id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BassStringProduct create(BassStringProduct product) {
        product.setCreatedAt(Instant.now());
        return repository.save(product);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BassStringProduct update(String id, BassStringProduct updates) {
        BassStringProduct existing = get(id);
        existing.setBrand(updates.getBrand());
        existing.setProductLine(updates.getProductLine());
        existing.setMaterial(updates.getMaterial());
        existing.setConstruction(updates.getConstruction());
        existing.setCoated(updates.isCoated());
        existing.setNumStrings(updates.getNumStrings());
        existing.setGauges(updates.getGauges());
        existing.setCompatibleScaleLengths(updates.getCompatibleScaleLengths());
        existing.setPriceRangeMinUsd(updates.getPriceRangeMinUsd());
        existing.setPriceRangeMaxUsd(updates.getPriceRangeMaxUsd());
        existing.setDescription(updates.getDescription());
        existing.setImageUrl(updates.getImageUrl());
        return repository.save(existing);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.forId("Bass string", id);
        }
        // Cascade: a removed product shouldn't leave orphaned reviews/comments behind.
        List<StringReview> reviews = mongoTemplate.find(
                Query.query(Criteria.where("bassStringId").is(id)), StringReview.class);
        List<String> reviewIds = reviews.stream().map(StringReview::getId).toList();
        if (!reviewIds.isEmpty()) {
            mongoTemplate.remove(Query.query(Criteria.where("reviewId").in(reviewIds)), ReviewComment.class);
        }
        reviewRepository.deleteAll(reviews);
        repository.deleteById(id);
    }
}
