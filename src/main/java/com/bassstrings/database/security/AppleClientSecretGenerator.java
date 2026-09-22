package com.bassstrings.database.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * "Sign in with Apple" does not issue a static client secret the way Google and
 * Facebook do. Instead, Apple expects the client to present a JWT, signed with the
 * ES256 private key downloaded from the Apple Developer portal, asserting who the
 * client is. Apple accepts a validity window of at most six months, so this class
 * mints one lazily and re-mints it a few minutes before it would expire.
 *
 * Requires {@code app.oauth2.apple.team-id}, {@code key-id}, {@code client-id}
 * (the Services ID) and {@code private-key-path} (the downloaded AuthKey_<keyId>.p8
 * file) - see README for how to obtain these from the Apple Developer portal.
 */
@Component
public class AppleClientSecretGenerator {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    private static final Duration VALIDITY = Duration.ofDays(150); // Apple's ceiling is ~6 months

    private final SocialLoginProperties.Apple properties;

    private volatile String cachedSecret;
    private volatile Instant cachedExpiry = Instant.EPOCH;

    public AppleClientSecretGenerator(SocialLoginProperties socialLoginProperties) {
        this.properties = socialLoginProperties.getApple();
    }

    public synchronized String getClientSecret() {
        if (cachedSecret == null || Instant.now().isAfter(cachedExpiry.minus(Duration.ofMinutes(10)))) {
            cachedSecret = generate();
        }
        return cachedSecret;
    }

    private String generate() {
        if (isBlank(properties.getTeamId()) || isBlank(properties.getKeyId())
                || isBlank(properties.getClientId()) || isBlank(properties.getPrivateKeyPath())) {
            throw new IllegalStateException(
                    "Apple sign-in is enabled but app.oauth2.apple.{team-id,key-id,client-id,private-key-path} "
                            + "are not fully configured. See README.md for how to obtain these from the Apple "
                            + "Developer portal, or remove 'apple' from app.oauth2.enabled-providers.");
        }
        try {
            ECPrivateKey privateKey = loadPrivateKey(properties.getPrivateKeyPath());
            Instant now = Instant.now();
            Instant expiry = now.plus(VALIDITY);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(properties.getTeamId())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .audience(APPLE_AUDIENCE)
                    .subject(properties.getClientId())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(properties.getKeyId())
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new ECDSASigner(privateKey));

            this.cachedExpiry = expiry;
            return jwt.serialize();
        } catch (GeneralSecurityException | com.nimbusds.jose.JOSEException e) {
            throw new IllegalStateException("Unable to sign Apple client_secret JWT", e);
        }
    }

    private ECPrivateKey loadPrivateKey(String path) throws GeneralSecurityException {
        String pem;
        try {
            pem = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read Apple private key at " + path, e);
        }
        String base64 = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(spec);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
