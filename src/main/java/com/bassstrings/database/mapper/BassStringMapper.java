package com.bassstrings.database.mapper;

import com.bassstrings.database.api.model.BassString;
import com.bassstrings.database.api.model.BassStringPage;
import com.bassstrings.database.api.model.BassStringRequest;
import com.bassstrings.database.api.model.PriceRange;
import com.bassstrings.database.domain.BassStringProduct;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.ArrayList;

/** Converts between the generated {@code api.model} DTOs and the persisted {@link BassStringProduct}. */
@Component
public class BassStringMapper {

    public BassString toDto(BassStringProduct entity) {
        PriceRange priceRange = null;
        if (entity.getPriceRangeMinUsd() != null || entity.getPriceRangeMaxUsd() != null) {
            priceRange = new PriceRange()
                    .minUsd(entity.getPriceRangeMinUsd())
                    .maxUsd(entity.getPriceRangeMaxUsd());
        }
        return new BassString()
                .id(entity.getId())
                .brand(entity.getBrand())
                .productLine(entity.getProductLine())
                .material(entity.getMaterial())
                .construction(entity.getConstruction())
                .coated(entity.isCoated())
                .numStrings(entity.getNumStrings())
                .gauges(entity.getGauges() == null ? null : new ArrayList<>(entity.getGauges()))
                .compatibleScaleLengths(entity.getCompatibleScaleLengths() == null
                        ? null : new ArrayList<>(entity.getCompatibleScaleLengths()))
                .priceRange(priceRange)
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().atOffset(ZoneOffset.UTC));
    }

    public BassStringProduct toEntity(BassStringRequest request) {
        BassStringProduct entity = new BassStringProduct();
        entity.setBrand(request.getBrand());
        entity.setProductLine(request.getProductLine());
        entity.setMaterial(request.getMaterial());
        entity.setConstruction(request.getConstruction());
        entity.setCoated(Boolean.TRUE.equals(request.getCoated()));
        entity.setNumStrings(request.getNumStrings());
        entity.setGauges(request.getGauges() == null ? new ArrayList<>() : new ArrayList<>(request.getGauges()));
        entity.setCompatibleScaleLengths(request.getCompatibleScaleLengths() == null
                ? new ArrayList<>() : new ArrayList<>(request.getCompatibleScaleLengths()));
        if (request.getPriceRange() != null) {
            entity.setPriceRangeMinUsd(request.getPriceRange().getMinUsd());
            entity.setPriceRangeMaxUsd(request.getPriceRange().getMaxUsd());
        }
        entity.setDescription(request.getDescription());
        entity.setImageUrl(request.getImageUrl());
        return entity;
    }

    public BassStringPage toPage(Page<BassStringProduct> page) {
        return new BassStringPage()
                .content(page.getContent().stream().map(this::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}
