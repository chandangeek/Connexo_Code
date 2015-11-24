package com.elster.partners.connexo.filters.generic;

import java.security.Principal;
import java.util.List;

/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoPrincipal implements Principal {
    final long userId;
    final String user;
    final List<String> roles;

    public ConnexoPrincipal(long userId, String user, List<String> roles) {
        this.userId = userId;
        this.user = user;
        this.roles = roles;
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
}
