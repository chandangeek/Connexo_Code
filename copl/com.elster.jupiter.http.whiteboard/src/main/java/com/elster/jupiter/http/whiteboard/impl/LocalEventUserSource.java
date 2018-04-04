package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.users.User;

public class LocalEventUserSource {
    User user;

    LocalEventUserSource(User user) {
        this.user = user;
    }

    public String getName() {
        return user.getName();
    }

    public long getVersion() {
        return user.getVersion();
    }

    public String getDescription() {
        return user.getDescription();
    }

    public String getLastSuccessfulLogin() {
        return user.getLastSuccessfulLogin().toString();
    }
}