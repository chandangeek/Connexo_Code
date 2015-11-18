package com.elster.partners.connexo.filters.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoAuthenticationRequestWrapper extends HttpServletRequestWrapper {

    ConnexoPrincipal principal;
    HttpServletRequest realRequest;

    public ConnexoAuthenticationRequestWrapper(ConnexoPrincipal principal, HttpServletRequest request) {
        super(request);
        this.principal = principal;
        this.realRequest = request;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (!this.principal.isValid()) {
            return false;
        }
        return this.principal.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        if (!this.principal.isValid()) {
            return null;
        }

        return this.principal;
    }
}
