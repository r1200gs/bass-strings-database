package com.bassstrings.database.service;

import com.bassstrings.database.api.model.GenreCount;
import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.PlayingStyleAverage;
import com.bassstrings.database.api.model.RatingSummary;
import com.bassstrings.database.api.model.Role;
import com.bassstrings.database.api.model.ReviewStatus;
import com.bassstrings.database.domain.BassStringProduct;
import com.bassstrings.database.domain.ReviewComment;
import com.bassstrings.database.domain.StringReview;
import com.bassstrings.database.domain.UserAccount;
import com.bassstrings.database.exception.ResourceNotFoundException;
import com.bassstrings.database.repository.BassStringProductRepository;
import com.bassstrings.database.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * Owns both the reviews themselves and the denormalized rating/suitability fields on
 * {@link BassStringProduct}, since those two things must never drift apart: every write
 * here that changes which reviews are PUBLISHED for a product ends with
 * {@link #recalculateAggregates(String)}.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final double SUITABLE_THRESHOLD = 4.0;

    private final ReviewRepository reviewRepository;
    private final BassStringProductRepository bassStringRepository;
    private final MongoTemplate mongoTemplate;

    public Page<StringReview> listForBassString(String bassStringId, Pageable pageable) {
        return reviewRepository.findByBassStringIdAndStatus(bassStringId, ReviewStatus.PUBLISHED, pageable);
    }

    public StringReview get(String id) {
        return reviewRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Review", id));
    }

    /** Reviews that named this bass model, regardless of which string product they're about - powers the "players' string choices for this bass" list on the model detail page. */
    public List<StringReview> listForBassModel(String bassModelId) {
        return reviewRepository.findByBassModelIdAndStatus(bassModelId, ReviewStatus.PUBLISHED);
    }

    @PreAuthorize("isAuthenticated()")
    public StringReview create(String bassStringId, StringReview review, UserAccount author) {
        if (!bassStringRepository.existsById(bassStringId)) {
            throw ResourceNotFoundException.forId("Bass string", bassStringId);
        }
        review.setBassStringId(bassStringId);
        review.setAuthorId(author.getId());
        review.setAuthorDisplayName(author.getDisplayName());
        review.setStatus(ReviewStatus.PUBLISHED);

        StringReview saved = reviewRepository.save(review);
        recalculateAggregates(bassStringId);
        return saved;
    }

    public StringReview update(String reviewId, StringReview updates, UserAccount currentUser) {
        StringReview existing = get(reviewId);
        requireAuthorOrAdmin(existing, currentUser);

        existing.setTitle(updates.getTitle());
        existing.setBody(updates.getBody());
        existing.setOverallRating(updates.getOverallRating());
        existing.setToneRating(updates.getToneRating());
        existing.setTensionRating(updates.getTensionRating());
        existing.setLongevityRating(updates.getLongevityRating());
        existing.setPlayingStyleRatings(updates.getPlayingStyleRatings());
        existing.setGenres(updates.getGenres());
        existing.setBassModelId(updates.getBassModelId());

        StringReview saved = reviewRepository.save(existing);
        recalculateAggregates(existing.getBassStringId());
        return saved;
    }

    public void delete(String reviewId, UserAccount currentUser) {
        StringReview existing = get(reviewId);
        requireAuthorOrModerator(existing, currentUser);

        mongoTemplate.remove(Query.query(Criteria.where("reviewId").is(reviewId)), ReviewComment.class);
        reviewRepository.deleteById(reviewId);
        recalculateAggregates(existing.getBassStringId());
    }

    public RatingSummary computeRatingSummary(String bassStringId) {
        List<StringReview> published = reviewRepository.findByBassStringIdAndStatus(bassStringId, ReviewStatus.PUBLISHED);

        RatingSummary summary = new RatingSummary()
                .bassStringId(bassStringId)
                .reviewCount(published.size())
                .averageOverall(average(published, StringReview::getOverallRating))
                .averageTone(average(published, StringReview::getToneRating))
                .averageTension(average(published, StringReview::getTensionRating))
                .averageLongevity(average(published, StringReview::getLongevityRating));

        Map<PlayingStyle, List<Integer>> ratingsByStyle = new EnumMap<>(PlayingStyle.class);
        Map<MusicGenre, Integer> genreCounts = new EnumMap<>(MusicGenre.class);
        for (StringReview review : published) {
            review.getPlayingStyleRatings().forEach((style, rating) ->
                    ratingsByStyle.computeIfAbsent(style, s -> new ArrayList<>()).add(rating));
            review.getGenres().forEach(genre -> genreCounts.merge(genre, 1, Integer::sum));
        }

        List<PlayingStyleAverage> byPlayingStyle = ratingsByStyle.entrySet().stream()
                .map(entry -> new PlayingStyleAverage()
                        .style(entry.getKey())
                        .averageRating(entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0))
                        .ratingCount(entry.getValue().size()))
                .toList();

        List<GenreCount> byGenre = genreCounts.entrySet().stream()
                .map(entry -> new GenreCount().genre(entry.getKey()).count(entry.getValue()))
                .toList();

        return summary.byPlayingStyle(byPlayingStyle).byGenre(byGenre);
    }

    /** Keeps {@link BassStringProduct}'s denormalized rating/suitability fields in sync. */
    private void recalculateAggregates(String bassStringId) {
        BassStringProduct product = bassStringRepository.findById(bassStringId).orElse(null);
        if (product == null) {
            return; // the product was deleted concurrently; nothing left to update
        }
        List<StringReview> published = reviewRepository.findByBassStringIdAndStatus(bassStringId, ReviewStatus.PUBLISHED);

        product.setReviewCount(published.size());
        product.setAverageOverallRating(average(published, StringReview::getOverallRating));

        Map<PlayingStyle, List<Integer>> ratingsByStyle = new EnumMap<>(PlayingStyle.class);
        Set<MusicGenre> genres = new LinkedHashSet<>();
        for (StringReview review : published) {
            review.getPlayingStyleRatings().forEach((style, rating) ->
                    ratingsByStyle.computeIfAbsent(style, s -> new ArrayList<>()).add(rating));
            genres.addAll(review.getGenres());
        }

        Set<PlayingStyle> suitable = new LinkedHashSet<>();
        ratingsByStyle.forEach((style, ratings) -> {
            double avg = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            if (avg >= SUITABLE_THRESHOLD) {
                suitable.add(style);
            }
        });

        product.setSuitablePlayingStyles(suitable);
        product.setTaggedGenres(genres);
        bassStringRepository.save(product);
    }

    private double average(List<StringReview> reviews, ToIntFunction<StringReview> extractor) {
        return reviews.stream().mapToInt(extractor).average().orElse(0.0);
    }

    private void requireAuthorOrAdmin(StringReview review, UserAccount currentUser) {
        boolean isAuthor = review.getAuthorId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().contains(Role.ADMIN);
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Only the review's author or an admin can edit it");
        }
    }

    private void requireAuthorOrModerator(StringReview review, UserAccount currentUser) {
        boolean isAuthor = review.getAuthorId().equals(currentUser.getId());
        boolean canModerate = currentUser.getRoles().contains(Role.ADMIN) || currentUser.getRoles().contains(Role.MONITOR);
        if (!isAuthor && !canModerate) {
            throw new AccessDeniedException("Only the review's author, a monitor, or an admin can remove it");
        }
    }
}
