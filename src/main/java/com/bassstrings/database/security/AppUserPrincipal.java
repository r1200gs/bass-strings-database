package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Principal used for OAuth2 (non-OIDC) providers, currently Facebook. Wraps the
 * provider's raw attributes plus our own {@link UserAccount} and its resulting
 * Spring Security authorities ("ROLE_USER", "ROLE_MONITOR", "ROLE_ADMIN", ...).
 */
public class AppUserPrincipal implements OAuth2User, AppPrincipal {

    private final UserAccount userAccount;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nameAttributeKey;

    public AppUserPrincipal(UserAccount userAccount,
                             Map<String, Object> attributes,
                             Collection<? extends GrantedAuthority> authorities,
                             String nameAttributeKey) {
        this.userAccount = userAccount;
        this.attributes = attributes;
        this.authorities = authorities;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public UserAccount getUserAccount() {
        return userAccount;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        Object value = attributes.get(nameAttributeKey);
        return value != null ? value.toString() : userAccount.getId();
    }
}
