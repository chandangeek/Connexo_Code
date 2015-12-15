package com.elster.partners.connexo.filters.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoAuthenticationRequestWrapper extends HttpServletRequestWrapper {

    private ConnexoPrincipal principal;
    private HttpServletRequest realRequest;
    private String authorizationToken;

    public ConnexoAuthenticationRequestWrapper(ConnexoPrincipal principal, HttpServletRequest request, String authorizationToken) {
        super(request);
        this.principal = principal;
        this.realRequest = request;
        this.authorizationToken = authorizationToken;
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

    @Override
    public String getHeader(String name){
        if(authorizationToken != null && name.equals("Authorization")){
            String realAuthorization = realRequest.getHeader(name);
            if(realAuthorization == null || !realAuthorization.startsWith("Bearer ")) {
                return "Bearer " + authorizationToken;
            }
        }

        return realRequest.getHeader(name);
    }
}
