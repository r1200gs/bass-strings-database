package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.ReviewStatus;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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
}
