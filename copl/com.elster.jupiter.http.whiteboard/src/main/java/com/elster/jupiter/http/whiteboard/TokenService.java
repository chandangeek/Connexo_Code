package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.users.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Map;

/**
 * By implementing this interface you can create
 * a TokenService which must have possibility to create, delete, invalidate
 * JWT (JSON Web Token) and also track the whole lifecycle of the token.
 *
 * @author edragutan
 */

@ProviderType
public interface TokenService {

    /**
     * Creates a signed JWT (JSON Web Token) with expiration date time used
     * from default configuration properties.
     *
     * @param customClaims - set of custom claims
     * @return the signed JWT for targeted user
     */
    SignedJWT createSignedJWT(User user, Map<String, Object> customClaims);

    /**
     * Creates a signed JWT (JSON Web Token) with specified expiration date time.
     *
     * @param customClaims       - set of custom claims
     * @param expirationDateTime - expiration date time
     * @return the signed JWT for targeted user
     */
    SignedJWT createSignedJWT(User user, Map<String, Object> customClaims, Instant expirationDateTime);

    /**
     * Validates a signed JWT (JSON Web Token)
     *
     * @param signedJWT - a signed JWT token
     * @throws JOSEException encryption exception in case of failed validation
     */
    void validateSignedJWT(SignedJWT signedJWT) throws JOSEException;

    /**
     * Invalidates a signed JWT (JSON Web Token)
     * <p>
     * This token can not be used for any further operations.
     *
     * @param signedJWT- a signed JWT token
     */
    void invalidateSignedJWT(SignedJWT signedJWT);

    void invalidateSignedJWTByUser(User user);

}
