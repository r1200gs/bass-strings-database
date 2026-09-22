package com.bassstrings.database.mapper;

import com.bassstrings.database.api.model.BassModel;
import com.bassstrings.database.api.model.BassModelPage;
import com.bassstrings.database.api.model.BassModelRequest;
import com.bassstrings.database.domain.BassGuitarModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

/** Converts between the generated {@code api.model} DTOs and the persisted {@link BassGuitarModel}. */
@Component
public class BassModelMapper {

    public BassModel toDto(BassGuitarModel entity) {
        return new BassModel()
                .id(entity.getId())
                .brand(entity.getBrand())
                .model(entity.getModel())
                .numStrings(entity.getNumStrings())
                .scaleLength(entity.getScaleLength())
                .yearIntroduced(entity.getYearIntroduced())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().atOffset(ZoneOffset.UTC));
    }

    public BassGuitarModel toEntity(BassModelRequest request) {
        BassGuitarModel entity = new BassGuitarModel();
        entity.setBrand(request.getBrand());
        entity.setModel(request.getModel());
        entity.setNumStrings(request.getNumStrings());
        entity.setScaleLength(request.getScaleLength());
        entity.setYearIntroduced(request.getYearIntroduced());
        entity.setDescription(request.getDescription());
        entity.setImageUrl(request.getImageUrl());
        return entity;
    }

    public BassModelPage toPage(Page<BassGuitarModel> page) {
        return new BassModelPage()
                .content(page.getContent().stream().map(this::toDto).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}
