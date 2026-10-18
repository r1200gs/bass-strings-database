package com.bassstrings.database.api.controller;

import com.bassstrings.database.api.UsersApi;
import com.bassstrings.database.api.model.User;
import com.bassstrings.database.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.bassstrings.database.security.CurrentUserSupport.currentUser;

/** Implements the {@link UsersApi} interface generated from openapi/bassstrings-api.yaml. */
@RestController
@RequiredArgsConstructor
public class UsersApiController implements UsersApi {

    private final UserMapper mapper;

    @Override
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(mapper.toDto(currentUser()));
    }
}
