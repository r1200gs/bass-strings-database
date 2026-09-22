package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.ScaleLength;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A catalog entry for a bass guitar model, e.g. "Fender American Professional II Jazz Bass". */
@Document(collection = "bass_models")
public class BassGuitarModel {

    @Id
    private String id;

    private String brand;

    private String model;

    private int numStrings;

    private ScaleLength scaleLength;

    private Integer yearIntroduced;

    private String description;

    private String imageUrl;

    @CreatedDate
    private Instant createdAt;

    public BassGuitarModel() {
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getNumStrings() {
        return numStrings;
    }

    public void setNumStrings(int numStrings) {
        this.numStrings = numStrings;
    }

    public ScaleLength getScaleLength() {
        return scaleLength;
    }

    public void setScaleLength(ScaleLength scaleLength) {
        this.scaleLength = scaleLength;
    }

    public Integer getYearIntroduced() {
        return yearIntroduced;
    }

    public void setYearIntroduced(Integer yearIntroduced) {
        this.yearIntroduced = yearIntroduced;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
