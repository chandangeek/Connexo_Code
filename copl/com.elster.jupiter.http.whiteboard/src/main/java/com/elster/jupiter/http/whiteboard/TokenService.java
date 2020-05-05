package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.users.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * By implementing this interface you can create
 * a TokenService which must have possibility to create, delete, invalidate
 * JWT (JSON Web Token) and also track the whole lifecycle of the token.
 *
 * @author edragutan
 */

@ProviderType
public interface TokenService<T> {

    /**
     * Initialize Token Service with set of keys and lifecycle information
     */
    void initialize(byte[] publicKey, byte[] privateKey, long tokenExpirationTime, long tokenRefreshThershold, long timeoutFrameToRefreshToken);

    /**
     * Creates a signed JWT (JSON Web Token) with expiration date time used
     * from default configuration properties.
     *
     * @param customClaims - set of custom claims
     * @return the signed JWT for targeted user
     */
    T createUserJWT(User user, Map<String, Object> customClaims) throws JOSEException;

    /**
     * Creates a signed JWT (JSON Web Token) for third party services that authorize
     * using OAuth 2.0 protocol
     */
    SignedJWT createServiceSignedJWT(User user, long expiresIn, String subject, String issuer, Map<String, Object> customClaims) throws JOSEException, ParseException;

    /**
     * Creates a permament signed JWT token (used by Flow)
     */
    SignedJWT createPermamentSignedJWT(User user) throws JOSEException;

    /**
     *
     */
    Optional<T> getUserJWT(UUID jwtId);

    /**
     * Validates a signed JWT (JSON Web Token)
     *
     * @param signedJWT - a signed JWT token
     * @throws JOSEException encryption exception in case of failed validation
     */
    TokenValidation validateSignedJWT(SignedJWT signedJWT) throws JOSEException, ParseException;


    /**
     * Validates a signed JWT (JSON Web Token) for service
     *
     * @param signedJWT - a signed JWT token
     * @throws JOSEException encryption exception in case of failed validation
     */
    TokenValidation validateServiceSignedJWT(SignedJWT signedJWT) throws JOSEException, ParseException;

    /**
     * Invalidates a signed JWT (JSON Web Token)
     * <p>
     * This token can not be used for any further operations.
     *
     * @param jwtId - a user which tokens are going to be invalidated
     */
    void invalidateUserJWT(final UUID jwtId) throws ExecutionException;

    void invalidateAllUserJWTsForUser(final User user);
}
