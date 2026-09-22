package com.bassstrings.database.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credentials for the three third-party login providers this app supports, bound from
 * {@code app.oauth2.*} in application.yml (backed by environment variables - see README).
 */
@ConfigurationProperties(prefix = "app.oauth2")
public class SocialLoginProperties {

    private Provider google = new Provider();
    private Provider facebook = new Provider();
    private Apple apple = new Apple();

    public Provider getGoogle() {
        return google;
    }

    public void setGoogle(Provider google) {
        this.google = google;
    }

    public Provider getFacebook() {
        return facebook;
    }

    public void setFacebook(Provider facebook) {
        this.facebook = facebook;
    }

    public Apple getApple() {
        return apple;
    }

    public void setApple(Apple apple) {
        this.apple = apple;
    }

    public static class Provider {
        private String clientId;
        private String clientSecret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    /**
     * Apple has no static client secret: it must be a short-lived JWT you sign yourself
     * with the private key from your Apple Developer "Sign in with Apple" key. See
     * {@link AppleClientSecretGenerator}.
     */
    public static class Apple {
        /** The Services ID registered with Apple, e.g. "com.bassstrings.hub.web". */
        private String clientId;
        /** Your 10-character Apple Developer Team ID. */
        private String teamId;
        /** The Key ID of the "Sign in with Apple" private key. */
        private String keyId;
        /** Filesystem path to the downloaded AuthKey_<keyId>.p8 file. */
        private String privateKeyPath;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getTeamId() {
            return teamId;
        }

        public void setTeamId(String teamId) {
            this.teamId = teamId;
        }

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getPrivateKeyPath() {
            return privateKeyPath;
        }

        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }
    }
}
