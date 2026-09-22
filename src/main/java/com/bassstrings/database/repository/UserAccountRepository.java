package com.bassstrings.database.repository;

import com.bassstrings.database.domain.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

    Optional<UserAccount> findByProviderAndProviderId(String provider, String providerId);

    Optional<UserAccount> findByEmail(String email);
}
