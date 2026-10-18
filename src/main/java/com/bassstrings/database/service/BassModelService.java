package com.bassstrings.database.service;

import com.bassstrings.database.domain.BassGuitarModel;
import com.bassstrings.database.exception.ResourceNotFoundException;
import com.bassstrings.database.repository.BassGuitarModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BassModelService {

    private final BassGuitarModelRepository repository;
    private final MongoTemplate mongoTemplate;

    public Page<BassGuitarModel> search(String brand, Integer numStrings, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();
        if (StringUtils.hasText(brand)) {
            criteria.add(Criteria.where("brand").regex(".*" + java.util.regex.Pattern.quote(brand) + ".*", "i"));
        }
        if (numStrings != null) {
            criteria.add(Criteria.where("numStrings").is(numStrings));
        }

        Query query = criteria.isEmpty() ? new Query() : new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        long total = mongoTemplate.count(query, BassGuitarModel.class);
        query.with(pageable);
        List<BassGuitarModel> content = mongoTemplate.find(query, BassGuitarModel.class);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
    }

    public BassGuitarModel get(String id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Bass model", id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BassGuitarModel create(BassGuitarModel model) {
        model.setCreatedAt(Instant.now());
        return repository.save(model);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BassGuitarModel update(String id, BassGuitarModel updates) {
        BassGuitarModel existing = get(id);
        existing.setBrand(updates.getBrand());
        existing.setModel(updates.getModel());
        existing.setNumStrings(updates.getNumStrings());
        existing.setScaleLength(updates.getScaleLength());
        existing.setYearIntroduced(updates.getYearIntroduced());
        existing.setDescription(updates.getDescription());
        existing.setImageUrl(updates.getImageUrl());
        return repository.save(existing);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException.forId("Bass model", id);
        }
        repository.deleteById(id);
    }
}
