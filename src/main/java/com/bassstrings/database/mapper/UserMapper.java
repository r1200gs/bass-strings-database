package com.bassstrings.database.mapper;

import com.bassstrings.database.api.model.User;
import com.bassstrings.database.domain.UserAccount;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.ArrayList;

/** Converts between the generated {@code api.model.User} DTO and the persisted {@link UserAccount}. */
@Component
public class UserMapper {

    public User toDto(UserAccount entity) {
        return new User()
                .id(entity.getId())
                .displayName(entity.getDisplayName())
                .email(entity.getEmail())
                .roles(new ArrayList<>(entity.getRoles()))
                .provider(entity.getProvider())
                .createdAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().atOffset(ZoneOffset.UTC));
    }
}
