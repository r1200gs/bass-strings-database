package com.bassstrings.database.security;

import com.bassstrings.database.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Handles the OIDC login flow, used here by Google and Apple (both send an
 * {@code id_token}, which is what makes a registration "OIDC" to Spring Security).
 *
 * Apple caveat: Apple only ever sends the user's name once, in a one-time
 * {@code user} form field on the very first authorization - never in the id_token
 * claims that {@link OidcUserService} reads here. So on Apple, {@code displayName}
 * below is usually null after the first sign-in and we fall back to the email
 * address. Capturing that one-time name would mean intercepting the raw
 * authorization response before Spring Security parses it; left as a follow-up
 * since it's not needed for the catalog/review MVP.
 */
@Service
@RequiredArgsConstructor
public class AccountProvisioningOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();
    private final UserAccountProvisioner provisioner;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String avatarUrl = oidcUser.getPicture();

        UserAccount account = provisioner.provision(registrationId, providerId, email, name, avatarUrl);
        return new AppOidcUserPrincipal(account, oidcUser, provisioner.authoritiesFor(account));
    }
}
