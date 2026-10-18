package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.BassStringsApi;
import com.bassstrings.database.api.model.BassString;
import com.bassstrings.database.api.model.BassStringPage;
import com.bassstrings.database.api.model.BassStringRequest;
import com.bassstrings.database.api.model.MusicGenre;
import com.bassstrings.database.api.model.PlayingStyle;
import com.bassstrings.database.api.model.RatingSummary;
import com.bassstrings.database.api.model.StringConstruction;
import com.bassstrings.database.api.model.StringMaterial;
import com.bassstrings.database.domain.BassStringProduct;
import com.bassstrings.database.mapper.BassStringMapper;
import com.bassstrings.database.service.BassStringSearchCriteria;
import com.bassstrings.database.service.BassStringService;
import com.bassstrings.database.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Implements the {@link BassStringsApi} interface generated from openapi/bassstrings-api.yaml. */
@RestController
@RequiredArgsConstructor
public class BassStringsApiController implements BassStringsApi {

    private final BassStringService bassStringService;
    private final ReviewService reviewService;
    private final BassStringMapper mapper;

    @Override
    public ResponseEntity<BassStringPage> listBassStrings(Integer page, Integer size, String brand,
                                                            StringMaterial material, StringConstruction construction,
                                                            Integer numStrings, PlayingStyle playingStyle,
                                                            MusicGenre genre, String query) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        BassStringSearchCriteria criteria = new BassStringSearchCriteria(
                brand, material, construction, numStrings, playingStyle, genre, query);
        return ResponseEntity.ok(mapper.toPage(bassStringService.search(criteria, pageable)));
    }

    @Override
    public ResponseEntity<BassString> createBassString(BassStringRequest bassStringRequest) {
        BassStringProduct created = bassStringService.create(mapper.toEntity(bassStringRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Override
    public ResponseEntity<BassString> getBassString(String bassStringId) {
        return ResponseEntity.ok(mapper.toDto(bassStringService.get(bassStringId)));
    }

    @Override
    public ResponseEntity<BassString> updateBassString(String bassStringId, BassStringRequest bassStringRequest) {
        BassStringProduct updated = bassStringService.update(bassStringId, mapper.toEntity(bassStringRequest));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteBassString(String bassStringId) {
        bassStringService.delete(bassStringId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RatingSummary> getRatingSummary(String bassStringId) {
        bassStringService.get(bassStringId); // 404s if the product doesn't exist
        return ResponseEntity.ok(reviewService.computeRatingSummary(bassStringId));
    }
}
