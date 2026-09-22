package com.bassstrings.database.mapper;

import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.PlayingStyleRating;
import com.bassstrings.database.api.model.Review;
import com.bassstrings.database.api.model.ReviewPage;
import com.bassstrings.database.api.model.ReviewRequest;
import com.bassstrings.database.domain.StringReview;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Converts between the generated {@code api.model} DTOs and the persisted {@link StringReview}. */
@Component
public class ReviewMapper {

    public Review toDto(StringReview entity) {
        List<PlayingStyleRating> styleRatings = entity.getPlayingStyleRatings().entrySet().stream()
                .map(e -> new PlayingStyleRating().style(e.getKey()).rating(e.getValue()))
                .toList();

        return new Review()
                .id(entity.getId())
                .bassStringId(entity.getBassStringId())
                .bassModelId(entity.getBassModelId())
                .authorId(entity.getAuthorId())
                .authorDisplayName(entity.getAuthorDisplayName())
                .title(entity.getTitle())
                .body(entity.getBody())
                .overallRating(entity.getOverallRating())
                .toneRating(entity.getToneRating())
                .tensionRating(entity.getTensionRating())
                .longevityRating(entity.getLongevityRating())
                .playingStyleRatings(styleRatings)
                .genres(new ArrayList<>(entity.getGenres()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }

    public StringReview toEntity(ReviewRequest request) {
        StringReview entity = new StringReview();
        entity.setBassModelId(request.getBassModelId());
        entity.setTitle(request.getTitle());
        entity.setBody(request.getBody());
        entity.setOverallRating(request.getOverallRating());
        entity.setToneRating(request.getToneRating());
        entity.setTensionRating(request.getTensionRating());
        entity.setLongevityRating(request.getLongevityRating());

        Map<PlayingStyle, Integer> ratings = new EnumMap<>(PlayingStyle.class);
        if (request.getPlayingStyleRatings() != null) {
            request.getPlayingStyleRatings().forEach(r -> ratings.put(r.getStyle(), r.getRating()));
        }
        entity.setPlayingStyleRatings(ratings);
        entity.setGenres(request.getGenres() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(request.getGenres()));
        return entity;
    }

    public ReviewPage toPage(Page<StringReview> page) {
        return new ReviewPage()
                .content(page.getContent().stream().map(this::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}
