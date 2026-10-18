package com.bassstrings.database.web;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.domain.StringReview;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Form-backing bean for the "write a review" Thymeleaf page. Kept separate from the
 * generated {@code ReviewRequest} DTO so the HTML form can use one plain, flat field per
 * playing style (easy for {@code th:field} to bind) instead of the indexed
 * {@code List<PlayingStyleRating>} the API uses; {@link #toPlayingStyleRatings()} folds
 * the flat fields back into that shape when the review is saved.
 */
@Getter
@Setter
public class ReviewFormData {

    private String title = "";
    private String body = "";
    private Integer overallRating;
    private Integer toneRating;
    private Integer tensionRating;
    private Integer longevityRating;

    private Integer fingerstyleRating;
    private Integer slapRating;
    private Integer pickRating;
    private Integer tappingRating;
    private Integer fingerpickingRating;
    private Integer thumpingRating;

    private Set<MusicGenre> genres = new LinkedHashSet<>();
    private String bassModelId;

    public Map<PlayingStyle, Integer> toPlayingStyleRatings() {
        Map<PlayingStyle, Integer> ratings = new EnumMap<>(PlayingStyle.class);
        putIfRated(ratings, PlayingStyle.FINGERSTYLE, fingerstyleRating);
        putIfRated(ratings, PlayingStyle.SLAP, slapRating);
        putIfRated(ratings, PlayingStyle.PICK, pickRating);
        putIfRated(ratings, PlayingStyle.TAPPING, tappingRating);
        putIfRated(ratings, PlayingStyle.FINGERPICKING, fingerpickingRating);
        putIfRated(ratings, PlayingStyle.THUMPING, thumpingRating);
        return ratings;
    }

    private void putIfRated(Map<PlayingStyle, Integer> ratings, PlayingStyle style, Integer value) {
        if (value != null && value >= 1 && value <= 5) {
            ratings.put(style, value);
        }
    }

    public StringReview toEntity() {
        StringReview review = new StringReview();
        review.setTitle(title);
        review.setBody(body);
        review.setOverallRating(overallRating != null ? overallRating : 0);
        review.setToneRating(toneRating != null ? toneRating : 0);
        review.setTensionRating(tensionRating != null ? tensionRating : 0);
        review.setLongevityRating(longevityRating != null ? longevityRating : 0);
        review.setPlayingStyleRatings(toPlayingStyleRatings());
        review.setGenres(genres);
        review.setBassModelId(bassModelId != null && !bassModelId.isBlank() ? bassModelId : null);
        return review;
    }
}
