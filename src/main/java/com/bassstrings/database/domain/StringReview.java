package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.ReviewStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** A subscriber's experience review of a {@link BassStringProduct}. */
@Document(collection = "reviews")
public class StringReview {

    @Id
    private String id;

    @Indexed
    private String bassStringId;

    private String bassModelId;

    @Indexed
    private String authorId;

    /** Denormalized at write time so review listings don't need a join to render. */
    private String authorDisplayName;

    private String title;

    private String body;

    private int overallRating;

    private int toneRating;

    private int tensionRating;

    private int longevityRating;

    /** Suitability rating (1-5) per playing style, e.g. SLAP -> 5, FINGERSTYLE -> 4. */
    private Map<PlayingStyle, Integer> playingStyleRatings = new EnumMap<>(PlayingStyle.class);

    private Set<MusicGenre> genres = new LinkedHashSet<>();

    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public StringReview() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBassStringId() {
        return bassStringId;
    }

    public void setBassStringId(String bassStringId) {
        this.bassStringId = bassStringId;
    }

    public String getBassModelId() {
        return bassModelId;
    }

    public void setBassModelId(String bassModelId) {
        this.bassModelId = bassModelId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
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

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public int getToneRating() {
        return toneRating;
    }

    public void setToneRating(int toneRating) {
        this.toneRating = toneRating;
    }

    public int getTensionRating() {
        return tensionRating;
    }

    public void setTensionRating(int tensionRating) {
        this.tensionRating = tensionRating;
    }

    public int getLongevityRating() {
        return longevityRating;
    }

    public void setLongevityRating(int longevityRating) {
        this.longevityRating = longevityRating;
    }

    public Map<PlayingStyle, Integer> getPlayingStyleRatings() {
        return playingStyleRatings;
    }

    public void setPlayingStyleRatings(Map<PlayingStyle, Integer> playingStyleRatings) {
        this.playingStyleRatings = playingStyleRatings;
    }

    public Set<MusicGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<MusicGenre> genres) {
        this.genres = genres;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
