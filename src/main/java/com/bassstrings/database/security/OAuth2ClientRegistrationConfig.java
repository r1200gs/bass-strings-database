package com.bassstrings.database.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the three OAuth2/OIDC client registrations explicitly rather than leaning on
 * Spring Boot's {@code spring.security.oauth2.client.registration.*} auto-configuration,
 * so every endpoint and scope in play is visible in one place - including Apple, whose
 * client secret can't be a static config value (see {@link AppleClientSecretGenerator}).
 * Each provider is only registered if its client id is configured, so the app still
 * starts (with that provider's login button simply absent) if you haven't set up all
 * three yet.
 */
@Configuration
@Slf4j
public class OAuth2ClientRegistrationConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(SocialLoginProperties properties,
                                                                       AppleClientSecretGenerator appleClientSecretGenerator) {
        List<ClientRegistration> registrations = new ArrayList<>();

        if (hasText(properties.getGoogle().getClientId())) {
            registrations.add(googleRegistration(properties.getGoogle()));
        } else {
            log.warn("Google sign-in disabled: app.oauth2.google.client-id is not set");
        }

        if (hasText(properties.getFacebook().getClientId())) {
            registrations.add(facebookRegistration(properties.getFacebook()));
        } else {
            log.warn("Facebook sign-in disabled: app.oauth2.facebook.client-id is not set");
        }

        if (hasText(properties.getApple().getClientId())) {
            registrations.add(appleRegistration(properties.getApple(), appleClientSecretGenerator));
        } else {
            log.warn("Apple sign-in disabled: app.oauth2.apple.client-id is not set");
        }

        if (properties.getMock().isEnabled()) {
            log.warn("Mock sign-in enabled against {} - local development and tests only",
                    properties.getMock().getIssuerUri());
            registrations.add(mockRegistration(properties.getMock()));
        } else if (registrations.isEmpty()) {
            // InMemoryClientRegistrationRepository refuses an empty list, so fall back to the
            // mock provider to keep the app bootable; its button just won't work unless a
            // mock-oauth2-server is running at app.oauth2.mock.issuer-uri.
            log.warn("No OAuth2 providers are configured. See README.md to set up Google, Facebook, and/or "
                    + "Apple sign-in, or set MOCK_OAUTH2_ENABLED=true to sign in via mock-oauth2-server.");
            registrations.add(mockRegistration(properties.getMock()));
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    /**
     * mock-oauth2-server serves a full OIDC provider under {@code <base>/<issuerId>}, so every
     * endpoint hangs off the issuer. The authorization endpoint is the one exception that may
     * use a different host, since the browser follows it rather than the app.
     */
    ClientRegistration mockRegistration(SocialLoginProperties.Mock props) {
        String issuer = trimTrailingSlash(props.getIssuerUri());
        String authorizationUri = hasText(props.getAuthorizationUri())
                ? props.getAuthorizationUri()
                : issuer + "/authorize";
        return ClientRegistration.withRegistrationId("mock")
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri(authorizationUri)
                .tokenUri(issuer + "/token")
                .userInfoUri(issuer + "/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri(issuer + "/jwks")
                .issuerUri(issuer)
                .clientName("Mock login")
                .build();
    }

    private ClientRegistration googleRegistration(SocialLoginProperties.Provider props) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .clientName("Google")
                .build();
    }

    private ClientRegistration facebookRegistration(SocialLoginProperties.Provider props) {
        return ClientRegistration.withRegistrationId("facebook")
                .clientId(props.getClientId())
                .clientSecret(props.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("public_profile", "email")
                .authorizationUri("https://www.facebook.com/v21.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v21.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,picture")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
    }

    private ClientRegistration appleRegistration(SocialLoginProperties.Apple props,
                                                   AppleClientSecretGenerator secretGenerator) {
        return ClientRegistration.withRegistrationId("apple")
                .clientId(props.getClientId())
                .clientSecret(secretGenerator.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "name", "email")
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .issuerUri("https://appleid.apple.com")
                .userNameAttributeName("sub")
                .clientName("Apple")
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
