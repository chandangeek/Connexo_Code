/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import java.security.Principal;
import java.util.List;

/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoPrincipal implements Principal {
    final private long userId;
    final private String user;
    final private List<String> roles;
    final private String token;

    public ConnexoPrincipal(long userId, String user, List<String> roles, String token) {
        this.userId = userId;
        this.user = user;
        this.roles = roles;
        this.token = token;
    }

    @Override
    public String getName() {
        return user;
    }

    public long getUserId(){
        return this.userId;
    }

    public List<String> getRoles() {
        return roles;
    }

    boolean isValid() {
        if(this.user == null || this.roles == null || this.roles.isEmpty()) {
            return false;
        }

        return true;
    }

    boolean isUserInRole(String role) {
        return this.roles.contains(role);
    }

    String getToken(){
        return this.token;
    }
}
