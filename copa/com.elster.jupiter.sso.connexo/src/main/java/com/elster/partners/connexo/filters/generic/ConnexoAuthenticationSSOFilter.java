/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoAuthenticationSSOFilter extends ConnexoAbstractSSOFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (shouldExcludUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ConnexoSecurityTokenManager securityManager = ConnexoSecurityTokenManager.getInstance(this.properties);
        ConnexoRestProxyManager restManager = ConnexoRestProxyManager.getInstance(getConnexoInternalUrl());

        String authorizationToken = getTokenFromCookie(request);

        if(authorizationToken == null) {
            authorizationToken = getTokenFromAuthorizationHeader(request);
        }

        ConnexoPrincipal principal = securityManager.verifyToken(authorizationToken, shouldRefreshToken(request));
        if(principal == null) {
            if(authorizationToken != null){
                updateToken(response, null, 0); // clear out token
            }

            HttpSession session = request.getSession();
            if(session != null){
                session.invalidate();
            }

            if(shouldUnauthorize(request)){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else {
                redirectToLogin(request, response);
            }
        }
        else {
            if(!principal.getToken().equals(authorizationToken)){
                updateToken(response, principal.getToken(), securityManager.getMaxAge());
            }

            filterChain.doFilter(new ConnexoAuthenticationRequestWrapper(principal, request, authorizationToken), response);
        }
    }

}