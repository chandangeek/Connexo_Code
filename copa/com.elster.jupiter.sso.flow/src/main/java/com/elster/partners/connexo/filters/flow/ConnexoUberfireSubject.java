/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow;

import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.commons.services.cdi.Veto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by dragos on 11/6/2015.
 */

@Veto
public class ConnexoUberfireSubject implements User {
    private final String user;
    private final Set<Group> groups;
    private final Set<Role> roles;

    ConnexoUberfireSubject(String user, Set<Group> groups, Set<Role> roles) {
        this.user = user;
        this.groups = groups;
        this.roles = roles;
    }

    @Override
    public String getIdentifier() {
        return user;
    }

    @Override
    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public Set<Group> getGroups() {
        return groups;
    }

    @Override
    public Map<String, String> getProperties() {
        return new HashMap<>();
    }

    @Override
    public void setProperty(String s, String s1) {

    }

    @Override
    public void removeProperty(String s) {

    }

    @Override
    public String getProperty(String s) {
        return null;
    }
}
