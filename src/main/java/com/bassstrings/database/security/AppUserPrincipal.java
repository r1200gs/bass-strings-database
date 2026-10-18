package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Principal used for OAuth2 (non-OIDC) providers, currently Facebook. Wraps the
 * provider's raw attributes plus our own {@link UserAccount} and its resulting
 * Spring Security authorities ("ROLE_USER", "ROLE_MONITOR", "ROLE_ADMIN", ...).
 */
@Getter
@RequiredArgsConstructor
public class AppUserPrincipal implements OAuth2User, AppPrincipal {

    private final UserAccount userAccount;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    @Getter(AccessLevel.NONE)
    private final String nameAttributeKey;

    @Override
    public String getName() {
        Object value = attributes.get(nameAttributeKey);
        return value != null ? value.toString() : userAccount.getId();
    }
}
