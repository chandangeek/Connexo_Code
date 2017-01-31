/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow.authorization;

import com.elster.partners.connexo.filters.flow.identity.ConnexoIdentityService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

/**
 * Created by dragos on 5/12/2016.
 */

@Alternative
@ApplicationScoped
public class ConnexoAuthenticationService implements AuthenticationService {
    private final ThreadLocal<User> userOnThisThread = new ThreadLocal<>();

    @Inject
    ConnexoIdentityService identityService;

    @Override
    public User login(String username, String password) {
        UserImpl user = new UserImpl(username, identityService.getSubject().getRoles(), identityService.getSubject().getGroups());
        userOnThisThread.set(user);
        return user;
    }

    @Override
    public boolean isLoggedIn() {
        return userOnThisThread.get() != null;
    }

    @Override
    public void logout() {
        userOnThisThread.remove();
    }

    @Override
    public User getUser() {
        User user = userOnThisThread.get();
        if (user == null) {
            return User.ANONYMOUS;
        }
        return user;
    }
}
