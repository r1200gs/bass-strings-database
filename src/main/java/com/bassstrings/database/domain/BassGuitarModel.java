package com.bassstrings.database.domain;

import com.bassstrings.database.api.model.ScaleLength;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** A catalog entry for a bass guitar model, e.g. "Fender American Professional II Jazz Bass". */
@Document(collection = "bass_models")
@Getter
@Setter
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
}
