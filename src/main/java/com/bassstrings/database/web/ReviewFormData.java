package com.bassstrings.database.web;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.domain.StringReview;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getToneRating() {
        return toneRating;
    }

    public void setToneRating(Integer toneRating) {
        this.toneRating = toneRating;
    }

    public Integer getTensionRating() {
        return tensionRating;
    }

    public void setTensionRating(Integer tensionRating) {
        this.tensionRating = tensionRating;
    }

    public Integer getLongevityRating() {
        return longevityRating;
    }

    public void setLongevityRating(Integer longevityRating) {
        this.longevityRating = longevityRating;
    }

    public Integer getFingerstyleRating() {
        return fingerstyleRating;
    }

    public void setFingerstyleRating(Integer fingerstyleRating) {
        this.fingerstyleRating = fingerstyleRating;
    }

    public Integer getSlapRating() {
        return slapRating;
    }

    public void setSlapRating(Integer slapRating) {
        this.slapRating = slapRating;
    }

    public Integer getPickRating() {
        return pickRating;
    }

    public void setPickRating(Integer pickRating) {
        this.pickRating = pickRating;
    }

    public Integer getTappingRating() {
        return tappingRating;
    }

    public void setTappingRating(Integer tappingRating) {
        this.tappingRating = tappingRating;
    }

    public Integer getFingerpickingRating() {
        return fingerpickingRating;
    }

    public void setFingerpickingRating(Integer fingerpickingRating) {
        this.fingerpickingRating = fingerpickingRating;
    }

    public Integer getThumpingRating() {
        return thumpingRating;
    }

    public void setThumpingRating(Integer thumpingRating) {
        this.thumpingRating = thumpingRating;
    }

    public Set<MusicGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<MusicGenre> genres) {
        this.genres = genres;
    }

    public String getBassModelId() {
        return bassModelId;
    }

    public void setBassModelId(String bassModelId) {
        this.bassModelId = bassModelId;
    }
}
