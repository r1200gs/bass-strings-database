package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.ScaleLength;
import com.bassstrings.database.api.model.StringConstruction;
import com.bassstrings.database.api.model.StringMaterial;
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

    public BassStringProduct() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProductLine() {
        return productLine;
    }

    public void setProductLine(String productLine) {
        this.productLine = productLine;
    }

    public StringMaterial getMaterial() {
        return material;
    }

    public void setMaterial(StringMaterial material) {
        this.material = material;
    }

    public StringConstruction getConstruction() {
        return construction;
    }

    public void setConstruction(StringConstruction construction) {
        this.construction = construction;
    }

    public boolean isCoated() {
        return coated;
    }

    public void setCoated(boolean coated) {
        this.coated = coated;
    }

    public int getNumStrings() {
        return numStrings;
    }

    public void setNumStrings(int numStrings) {
        this.numStrings = numStrings;
    }

    public List<Double> getGauges() {
        return gauges;
    }

    public void setGauges(List<Double> gauges) {
        this.gauges = gauges;
    }

    public List<ScaleLength> getCompatibleScaleLengths() {
        return compatibleScaleLengths;
    }

    public void setCompatibleScaleLengths(List<ScaleLength> compatibleScaleLengths) {
        this.compatibleScaleLengths = compatibleScaleLengths;
    }

    public Double getPriceRangeMinUsd() {
        return priceRangeMinUsd;
    }

    public void setPriceRangeMinUsd(Double priceRangeMinUsd) {
        this.priceRangeMinUsd = priceRangeMinUsd;
    }

    public Double getPriceRangeMaxUsd() {
        return priceRangeMaxUsd;
    }

    public void setPriceRangeMaxUsd(Double priceRangeMaxUsd) {
        this.priceRangeMaxUsd = priceRangeMaxUsd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getAverageOverallRating() {
        return averageOverallRating;
    }

    public void setAverageOverallRating(double averageOverallRating) {
        this.averageOverallRating = averageOverallRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<PlayingStyle> getSuitablePlayingStyles() {
        return suitablePlayingStyles;
    }

    public void setSuitablePlayingStyles(Set<PlayingStyle> suitablePlayingStyles) {
        this.suitablePlayingStyles = suitablePlayingStyles;
    }

    public Set<MusicGenre> getTaggedGenres() {
        return taggedGenres;
    }

    public void setTaggedGenres(Set<MusicGenre> taggedGenres) {
        this.taggedGenres = taggedGenres;
    }
}
