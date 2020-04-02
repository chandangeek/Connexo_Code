package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;

/**
 * Use this interface to define your own
 * implementation of SAML Single Logout Flow.
 */
@ProviderType
public interface SAMLSingleLogoutService {

    /**
     * Method reponsible for imidiat invalidation of user's session/token.
     *
     * @param logoutRequest
     * @return response with the status of logout
     */
    LogoutResponse initializeSingleLogout(LogoutRequest logoutRequest);
}
