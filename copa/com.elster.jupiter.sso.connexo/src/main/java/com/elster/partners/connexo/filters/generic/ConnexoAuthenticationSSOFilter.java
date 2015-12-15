package com.elster.partners.connexo.filters.generic;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

        String authorizationToken = getTokenFromCookie(request);

        if(authorizationToken == null) {
            authorizationToken = getTokenFromAuthorizationHeader(request);
        }

        if(authorizationToken == null || !securityManager.verifyToken(authorizationToken)) {
            if(authorizationToken != null){
                updateToken(response, null, 0); // clear out token
            }

            if(shouldUnauthorize(request)){
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else {
                redirectToLogin(request, response);
            }
        }
        else {
            if(securityManager.needToUpdateToken()){
                updateToken(response, securityManager.getUpdatedToken(), securityManager.getMaxAge());
            }

            filterChain.doFilter(new ConnexoAuthenticationRequestWrapper(securityManager.getPrincipal(), request, authorizationToken), response);
        }
    }

}