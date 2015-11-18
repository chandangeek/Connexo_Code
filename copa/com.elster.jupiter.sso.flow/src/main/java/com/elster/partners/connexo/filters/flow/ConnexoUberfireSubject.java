package com.elster.partners.connexo.filters.flow;

import org.jboss.solder.core.Veto;
import org.uberfire.security.Role;
import org.uberfire.security.Subject;
import org.uberfire.security.impl.RoleImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dragos on 11/6/2015.
 */

@Veto
public class ConnexoUberfireSubject implements Subject {
    private final String user;
    private final List<Role> roles;

    ConnexoUberfireSubject(String user, List<Role> roles){
        this.user = user;
        this.roles = roles;
    }

    @Override
    public List<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean hasRole(Role role) {
        return true;
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>();
    }

    @Override
    public void aggregateProperty(String s, String s1) {

    }

    @Override
    public void removeProperty(String s) {

    }

    @Override
    public String getProperty(String s, String s1) {
        return null;
    }

    @Override
    public String getName() {
        return this.user;
    }
}
