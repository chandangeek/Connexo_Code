package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.users.User;

import java.util.Objects;
import java.util.Optional;

public class TokenValidation {

    private final boolean valid;
    private final User user;
    private final String token;

    public TokenValidation(boolean valid, User user, String token) {
        this.valid = valid;
        this.user = user;
        this.token = token;
    }

    public boolean isValid() {
        return valid;
    }

    public Optional<User> getUser() {
        if (isValid() || (Objects.nonNull(user) && user.isRoleModified())) {
            return Optional.ofNullable(user);
        } else {
            return Optional.empty();
        }
    }

    public String getToken() {
        if (valid) {
            return token;
        } else {
            return null;
        }
    }
}