package com.bassstrings.database.security;

import lombok.AccessLevel;
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
    @Setter(AccessLevel.NONE)
    private Provider mock = new Provider();

    @Getter
    @Setter
    public static class Provider {
        private String clientId;
        private String clientSecret;
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
