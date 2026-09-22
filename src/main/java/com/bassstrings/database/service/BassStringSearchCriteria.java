package com.bassstrings.database.service;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.StringConstruction;
import com.bassstrings.database.api.model.StringMaterial;

/** Every field is optional; {@link BassStringService#search} combines whichever are set. */
public record BassStringSearchCriteria(
        String brand,
        StringMaterial material,
        StringConstruction construction,
        Integer numStrings,
        PlayingStyle playingStyle,
        MusicGenre genre,
        String freeTextQuery
) {
    public static BassStringSearchCriteria empty() {
        return new BassStringSearchCriteria(null, null, null, null, null, null, null);
    }
}
