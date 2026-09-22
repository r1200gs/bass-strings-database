package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Principal used for OIDC providers: Google and Apple both return an {@code id_token},
 * so Spring Security routes them through {@code oidcUserService(...)} rather than the
 * plain {@code userService(...)} hook used for Facebook. See {@link AppUserPrincipal}.
 */
public class AppOidcUserPrincipal implements OidcUser, AppPrincipal {

    private final UserAccount userAccount;
    private final OidcUser delegate;
    private final Collection<? extends GrantedAuthority> authorities;

    public AppOidcUserPrincipal(UserAccount userAccount,
                                 OidcUser delegate,
                                 Collection<? extends GrantedAuthority> authorities) {
        this.userAccount = userAccount;
        this.delegate = delegate;
        this.authorities = authorities;
    }

    @Override
    public UserAccount getUserAccount() {
        return userAccount;
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    /** Unused by us directly, but part of the OidcUser contract via OAuth2AuthenticatedPrincipal. */
    public OAuth2User getDelegate() {
        return delegate;
    }
}
