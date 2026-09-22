package com.bassstrings.database.web;

import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.domain.StringReview;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Plain-unit-test coverage for the flat-fields-to-Map folding in {@link ReviewFormData},
 * since that's the one piece of non-trivial logic in the web form layer. Doesn't need a
 * Spring context or a database - see README.md for how to run the full app against Mongo.
 */
class ReviewFormDataTest {

    @Test
    void onlyRatedPlayingStylesAreIncluded() {
        ReviewFormData form = new ReviewFormData();
        form.setSlapRating(5);
        form.setFingerstyleRating(3);
        // tapping/pick/fingerpicking/thumbing left null: the player didn't rate them

        var ratings = form.toPlayingStyleRatings();

        assertEquals(2, ratings.size());
        assertEquals(5, ratings.get(PlayingStyle.SLAP));
        assertEquals(3, ratings.get(PlayingStyle.FINGERSTYLE));
        assertFalse(ratings.containsKey(PlayingStyle.TAPPING));
    }

    @Test
    void outOfRangeRatingsAreIgnored() {
        ReviewFormData form = new ReviewFormData();
        form.setPickRating(0);
        form.setThumpingRating(9);

        var ratings = form.toPlayingStyleRatings();

        assertFalse(ratings.containsKey(PlayingStyle.PICK));
        assertFalse(ratings.containsKey(PlayingStyle.THUMPING));
    }

    @Test
    void toEntityCarriesCoreFieldsAndBlankBassModelIdBecomesNull() {
        ReviewFormData form = new ReviewFormData();
        form.setTitle("Great low tension");
        form.setBody("Really comfortable for slap.");
        form.setOverallRating(5);
        form.setToneRating(4);
        form.setTensionRating(5);
        form.setLongevityRating(4);
        form.setGenres(Set.of(MusicGenre.FUNK));
        form.setBassModelId("   ");

        StringReview entity = form.toEntity();

        assertEquals("Great low tension", entity.getTitle());
        assertEquals(5, entity.getOverallRating());
        assertEquals(Set.of(MusicGenre.FUNK), entity.getGenres());
        assertNull(entity.getBassModelId());
    }
}
