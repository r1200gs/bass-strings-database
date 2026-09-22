package com.bassstrings.database.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Apple requires {@code response_mode=form_post} on the authorization request whenever
 * the "name" or "email" scopes are requested (which we always do, to provision an
 * account) - otherwise its authorization server rejects the request. Spring Security's
 * default resolver has no built-in way to add that, so this wraps it and appends the
 * parameter only for the Apple registration, identified by its authorization endpoint.
 */
public class AppleAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;

    public AppleAwareAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                                    String authorizationRequestBaseUri) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return withAppleFormPost(delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return withAppleFormPost(delegate.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest withAppleFormPost(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }
        if (!authorizationRequest.getAuthorizationRequestUri().contains("appleid.apple.com")) {
            return authorizationRequest;
        }
        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("response_mode", "form_post");
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
