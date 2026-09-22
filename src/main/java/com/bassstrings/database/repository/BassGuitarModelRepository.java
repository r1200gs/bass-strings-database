package com.bassstrings.database.repository;

import com.bassstrings.database.domain.BassGuitarModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BassGuitarModelRepository extends MongoRepository<BassGuitarModel, String> {
}
