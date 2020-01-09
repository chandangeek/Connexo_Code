package com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.impl;

import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.TokenService;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenRequest;
import com.elster.jupiter.webservice.inbound.rest.scim.impl.oauth.dto.TokenResponse;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class TokenServiceImpl implements TokenService {

    private static final SecretKey PRIVATE_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

    private static final char[] subject = new char[]{'e', 'n', 'e', 'x', 'i', 's'};
    private static final char[] tokenType = new char[]{'b', 'e', 'a', 'r', 'e', 'r'};

    @Override
    public TokenResponse createTokenResponse(final TokenRequest tokenRequest) {
        return TokenResponse.TokenResponseBuilder.aTokenResponse()
                .withAccessToken(createJwsWithSignatureAlgorith(signatureAlgorithm))
                .withTokenType(String.valueOf(tokenType))
                .withExpirationDate(getExpirationDate().toString())
                .build();
    }

    @Override
    public Jwt verifyToken(final String jws) {
        return Jwts.parser().setSigningKey(PRIVATE_KEY).parse(jws);
    }

    private String createJwsWithSignatureAlgorith(final SignatureAlgorithm signatureAlgorithm) {
        return Jwts.builder()
                .setSubject(String.valueOf(subject))
                .setExpiration(getExpirationDate())
                .setIssuedAt(new Date())
                .signWith(PRIVATE_KEY)
                .compact();
    }

    private Date getExpirationDate() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long tenMinutesTimeMillis = 30000 * 1000;
        final long expirationTimeMillis = currentTimeMillis + tenMinutesTimeMillis;
        return new Date(expirationTimeMillis);
    }
}
