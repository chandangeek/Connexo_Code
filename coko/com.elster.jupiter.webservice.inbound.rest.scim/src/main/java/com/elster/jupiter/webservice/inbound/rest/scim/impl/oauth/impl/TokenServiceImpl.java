package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.impl;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public class TokenServiceImpl implements TokenService {

    // TODO: Move Private Key to HSM/Data Vault storage, plus implement rotation logic.
    private static final SecretKey PRIVATE_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    // TODO: Move these claims to other class
    private static final char[] ISS = new char[]{'c', 'o', 'n', 'n', 'e', 'x', 'o'};
    private static final char[] SUBJECT = new char[]{'e', 'n', 'e', 'x', 'i', 's'};
    private static final char[] TOKEN_TYPE = new char[]{'b', 'e', 'a', 'r', 'e', 'r'};

    @Override
    public TokenResponse createToken() {
        final Instant expirationDate = getExpirationDate();

        return TokenResponse.TokenResponseBuilder.aTokenResponse()
                .withAccessToken(createJws(expirationDate))
                .withTokenType(String.valueOf(TOKEN_TYPE))
                .withExpiresIn(expirationDate.getEpochSecond())
                .build();
    }

    @Override
    public Jwt<?, ?> verifyToken(final String jws) {
        return Jwts.parser().setSigningKey(PRIVATE_KEY).parse(jws);
    }

    private String createJws(final Instant expirationDate) {
        return Jwts.builder()
                .setIssuer(String.valueOf(ISS))
                .setIssuedAt(new Date())
                .setNotBefore(new Date())
                .setSubject(String.valueOf(SUBJECT))
                .setExpiration(Date.from(expirationDate))
                .signWith(PRIVATE_KEY)
                .compact();
    }

    private Instant getExpirationDate() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long tenMinutesTimeMillis = 30000 * 1000;
        final long expirationTimeMillis = currentTimeMillis + tenMinutesTimeMillis;
        return Instant.ofEpochMilli(expirationTimeMillis);
    }
}
