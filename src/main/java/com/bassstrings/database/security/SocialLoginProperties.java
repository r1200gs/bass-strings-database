package com.bassstrings.database.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials for the three third-party login providers this app supports, bound from
 * {@code app.oauth2.*} in application.yml (backed by environment variables - see README).
 */
@ConfigurationProperties(prefix = "app.oauth2")
@Getter
@Setter
public class SocialLoginProperties {

    private Provider google = new Provider();
    private Provider facebook = new Provider();
    private Apple apple = new Apple();
    private Mock mock = new Mock();

    @Getter
    @Setter
    public static class Provider {
        private String clientId;
        private String clientSecret;
    }

    /**
     * A local stand-in provider backed by navikt's mock-oauth2-server (see
     * docker-compose.yml), for signing in during local runs and integration tests without
     * real Google/Facebook/Apple credentials. Never enable this in production.
     */
    @Getter
    @Setter
    public static class Mock extends Provider {
        private boolean enabled;
        /**
         * Issuer the app talks to server-to-server (token, userinfo, JWKS), e.g.
         * {@code http://mock-oauth2-server:8080/default} from inside docker-compose. It must
         * match the {@code iss} claim the mock server puts in its id_tokens, which it
         * derives from the host the token request was sent to.
         */
        private String issuerUri;
        /**
         * Authorization endpoint the browser is redirected to. Only differs from
         * {@code issuerUri + "/authorize"} when the app and the browser reach the mock
         * server by different host names (the app container vs. your machine). Defaults
         * to {@code issuerUri + "/authorize"} when blank.
         */
        private String authorizationUri;
    }

    /**
     * Apple has no static client secret: it must be a short-lived JWT you sign yourself
     * with the private key from your Apple Developer "Sign in with Apple" key. See
     * {@link AppleClientSecretGenerator}.
     */
    @Getter
    @Setter
    public static class Apple {
        /** The Services ID registered with Apple, e.g. "com.bassstrings.hub.web". */
        private String clientId;
        /** Your 10-character Apple Developer Team ID. */
        private String teamId;
        /** The Key ID of the "Sign in with Apple" private key. */
        private String keyId;
        /** Filesystem path to the downloaded AuthKey_<keyId>.p8 file. */
        private String privateKeyPath;
    }
}
