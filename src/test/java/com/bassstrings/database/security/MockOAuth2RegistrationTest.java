package com.bassstrings.database.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the mock-oauth2-server registration points every endpoint at the configured
 * issuer, and that the browser-facing authorization endpoint can be overridden for the
 * docker-compose setup where the app and the browser use different host names.
 */
class MockOAuth2RegistrationTest {

    private final OAuth2ClientRegistrationConfig config = new OAuth2ClientRegistrationConfig();

    @Test
    void endpointsHangOffTheIssuer() {
        ClientRegistration registration = config.mockRegistration(mock("http://localhost:8090/default/", null));
        ClientRegistration.ProviderDetails provider = registration.getProviderDetails();

        assertEquals("mock", registration.getRegistrationId());
        assertEquals("http://localhost:8090/default", provider.getIssuerUri());
        assertEquals("http://localhost:8090/default/authorize", provider.getAuthorizationUri());
        assertEquals("http://localhost:8090/default/token", provider.getTokenUri());
        assertEquals("http://localhost:8090/default/userinfo", provider.getUserInfoEndpoint().getUri());
        assertEquals("http://localhost:8090/default/jwks", provider.getJwkSetUri());
    }

    @Test
    void authorizationUriCanPointTheBrowserElsewhere() {
        ClientRegistration registration = config.mockRegistration(
                mock("http://mock-oauth2-server:8080/default", "http://localhost:8090/default/authorize"));
        ClientRegistration.ProviderDetails provider = registration.getProviderDetails();

        assertEquals("http://localhost:8090/default/authorize", provider.getAuthorizationUri());
        assertEquals("http://mock-oauth2-server:8080/default/token", provider.getTokenUri());
        assertEquals("http://mock-oauth2-server:8080/default", provider.getIssuerUri());
    }

    private SocialLoginProperties.Mock mock(String issuerUri, String authorizationUri) {
        SocialLoginProperties.Mock mock = new SocialLoginProperties.Mock();
        mock.setEnabled(true);
        mock.setClientId("bass-strings-database");
        mock.setClientSecret("mock-secret");
        mock.setIssuerUri(issuerUri);
        mock.setAuthorizationUri(authorizationUri);
        return mock;
    }
}
