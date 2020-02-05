package com.elster.jupiter.http.whiteboard.impl.token;

import com.elster.jupiter.users.User;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;

public final class UserJWT {

    private final User user;
    private final SignedJWT signedJWT;
    private final Instant expirationDate;

    public UserJWT(final User user, final SignedJWT signedJWT, final Instant expirationDate) {
        this.user = user;
        this.signedJWT = signedJWT;
        this.expirationDate = expirationDate;
    }

    public User getUser() {
        return user;
    }

    public SignedJWT getSignedJWT() {
        return signedJWT;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }
}
