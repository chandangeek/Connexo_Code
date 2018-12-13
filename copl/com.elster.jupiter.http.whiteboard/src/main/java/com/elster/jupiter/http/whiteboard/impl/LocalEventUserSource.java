package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.users.User;

public class LocalEventUserSource {

    private User user;
    private String userName;
    
    LocalEventUserSource(User user) {
        this.user = user;
    }

    LocalEventUserSource(String userName) {
        this.userName = userName;
    }

    public String getName() {
        if (user != null) {
            return user.getName();
        }

        return userName;
    }

    public long getVersion() {
        if (user != null) {
            return user.getVersion();
        }

        return 0L;
    }

    public String getDescription() {
        if (user != null) {
            return user.getDescription();
        }

        return "";
    }

    public String getLastSuccessfulLogin() {
        if (user != null) {
            return user.getLastSuccessfulLogin().toString();
        }

        return "";
    }

}
