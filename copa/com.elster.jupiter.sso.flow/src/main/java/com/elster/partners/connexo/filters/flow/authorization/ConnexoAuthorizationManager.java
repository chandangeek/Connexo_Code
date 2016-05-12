package com.elster.partners.connexo.filters.flow.authorization;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.uberfire.commons.services.cdi.Veto;
import org.uberfire.security.Resource;
import org.uberfire.security.authz.AuthorizationManager;

/**
 * Created by dragos on 11/9/2015.
 * This class purpose is just to suspend role-based access in jBPM Web Console
 * In Connexo Flow, only full application access is possible
 * Note: in jBPM 6.1, it will not work properly due to a bug
 * Alternative setting: org.uberfire.io.auth = com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthorizationManager
 */

@Veto
public class ConnexoAuthorizationManager implements AuthorizationManager {
    @Override
    public boolean supports(Resource resource) {
        return true;
    }

    @Override
    public boolean authorize(Resource resource, User subject) throws UnauthorizedException {
        return true;
    }
}
