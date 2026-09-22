package com.bassstrings.database.repository;

import com.bassstrings.database.domain.BassStringProduct;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BassStringProductRepository extends MongoRepository<BassStringProduct, String> {
}
