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

    private String getTokenFromCookie(HttpServletRequest request) {
        String authorizationToken = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("X-CONNEXO-TOKEN")) {
                    authorizationToken = cookies[i].getValue();
                    break;
                }
            }
        }
        return authorizationToken;
    }

    private String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationToken = null;
        String authorization = request.getHeader("Authorization");
        if(authorization != null){
            if(authorization.startsWith("Bearer ")) {
                authorizationToken = authorization.split(" ")[1];
            }
            else if (authorization.startsWith("Basic ")){
                ConnexoRestProxyManager restManager = ConnexoRestProxyManager.getInstance(getConnexoInternalUrl(), authorization);
                authorizationToken = restManager.getConnexoAuthorizationToken();
            }
        }
        return authorizationToken;
    }

    private void updateToken(HttpServletResponse response, String newValue, int maxAge) {
        response.setHeader("X-AUTH-TOKEN", newValue);

        Cookie tokenCookie = new Cookie("X-CONNEXO-TOKEN", newValue);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(maxAge); // in seconds
        tokenCookie.setHttpOnly(true);
        response.addCookie(tokenCookie);
    }
}