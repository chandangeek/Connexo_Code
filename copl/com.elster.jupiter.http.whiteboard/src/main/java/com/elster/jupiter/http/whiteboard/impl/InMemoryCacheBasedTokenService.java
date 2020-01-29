package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.TokenService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.osgi.service.component.annotations.Component;

import java.time.Instant;
import java.util.Map;

@Component(
        name = "com.elster.jupiter.http.whiteboard.InMemoryCacheBasedTokenService",
        property = {
                "name=" + InMemoryCacheBasedTokenService.SERVICE_NAME
        },
        immediate = true,
        service = {
                TokenService.class
        }
)
public class InMemoryCacheBasedTokenService implements TokenService {

    public static final String SERVICE_NAME = "TNS";

    @Override
    public SignedJWT createSignedJWT(Map<String, Object> customClaims) {
        return null;
    }

    @Override
    public SignedJWT createSignedJWT(Map<String, Object> customClaims, Instant expirationDateTime) {
        return null;
    }

    @Override
    public void validateSignedJWT(SignedJWT signedJWT) throws JOSEException {

    }

    @Override
    public void invalidateSignedJWT(SignedJWT signedJWT) {

    }
}
