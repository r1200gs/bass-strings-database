package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.ScaleLength;
import com.bassstrings.database.api.model.StringConstruction;
import com.bassstrings.database.api.model.StringMaterial;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A bass string product on the market, e.g. "D'Addario XL170 Nickel Wound".
 *
 * {@code averageOverallRating} and {@code reviewCount} are denormalized aggregates,
 * kept in sync by {@link com.bassstrings.hub.service.ReviewService} whenever a review
 * for this product is created, edited, or removed, so the catalog can sort/filter by
 * rating without an aggregation query on every page view.
 */
@Document(collection = "bass_strings")
@Getter
@Setter
public class BassStringProduct {

    @Id
    private String id;

    @Indexed
    private String brand;

    private String productLine;

    private StringMaterial material;

    private StringConstruction construction;

    private boolean coated;

    private int numStrings;

    private List<Double> gauges = new ArrayList<>();

    private List<ScaleLength> compatibleScaleLengths = new ArrayList<>();

    private Double priceRangeMinUsd;

    private Double priceRangeMaxUsd;

    private String description;

    private String imageUrl;

    private double averageOverallRating;

    private int reviewCount;

    /**
     * Denormalized from published reviews by {@link com.bassstrings.hub.service.ReviewService}:
     * playing styles whose average suitability rating across this product's reviews is >= 4.
     * Recomputed, not user-editable, so catalog search-by-playing-style stays a plain field
     * query instead of an aggregation join on every page view.
     */
    private Set<PlayingStyle> suitablePlayingStyles = new LinkedHashSet<>();

    /** Denormalized union of genres tagged across this product's published reviews. */
    private Set<MusicGenre> taggedGenres = new LinkedHashSet<>();

    @CreatedDate
    private Instant createdAt;
}
