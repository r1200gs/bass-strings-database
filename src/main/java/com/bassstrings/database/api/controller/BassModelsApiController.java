package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.BassModelsApi;
import com.bassstrings.database.api.model.BassModel;
import com.bassstrings.database.api.model.BassModelPage;
import com.bassstrings.database.api.model.BassModelRequest;
import com.bassstrings.database.domain.BassGuitarModel;
import com.bassstrings.database.mapper.BassModelMapper;
import com.bassstrings.database.service.BassModelService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements the {@link BassModelsApi} interface generated from openapi/bassstrings-api.yaml
 * by the openapi-generator-maven-plugin (see pom.xml) at {@code generate-sources} time.
 * Method-level authorization (admin-only writes) is enforced in {@link BassModelService}.
 */
@RestController
public class BassModelsApiController implements BassModelsApi {

    private final BassModelService service;
    private final BassModelMapper mapper;

    public BassModelsApiController(BassModelService service, BassModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<BassModelPage> listBassModels(Integer page, Integer size, String brand, Integer numStrings) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(mapper.toPage(service.search(brand, numStrings, pageable)));
    }

    @Override
    public ResponseEntity<BassModel> createBassModel(BassModelRequest bassModelRequest) {
        BassGuitarModel created = service.create(mapper.toEntity(bassModelRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(created));
    }

    @Override
    public ResponseEntity<BassModel> getBassModel(String bassModelId) {
        return ResponseEntity.ok(mapper.toDto(service.get(bassModelId)));
    }

    @Override
    public ResponseEntity<BassModel> updateBassModel(String bassModelId, BassModelRequest bassModelRequest) {
        BassGuitarModel updated = service.update(bassModelId, mapper.toEntity(bassModelRequest));
        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @Override
    public ResponseEntity<Void> deleteBassModel(String bassModelId) {
        service.delete(bassModelId);
        return ResponseEntity.noContent().build();
    }
}
