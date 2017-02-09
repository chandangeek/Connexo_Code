package com.elster.partners.connexo.filters.flow.authorization;

import com.elster.partners.connexo.filters.flow.identity.ConnexoFlowRestProxyService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.uberfire.backend.server.security.IOSecurityAuth;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

@Alternative
@ApplicationScoped
@IOSecurityAuth
public class ConnexoAuthenticationService implements AuthenticationService {
    private final ThreadLocal<User> userOnThisThread = new ThreadLocal<>();

    @Inject
    ConnexoFlowRestProxyService connexoFlowRestProxyService;

    @Override
    public User login(String username, String password) {
        User user = userOnThisThread.get();
        if (user == null) {
            user = connexoFlowRestProxyService.authenticate(username, password);
            if (user == null) {
                userOnThisThread.set(user);
            }
        }

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

    public void setUser(User user) {
        if (userOnThisThread.get() != user) {
            userOnThisThread.set(user);
        }
    }
}
