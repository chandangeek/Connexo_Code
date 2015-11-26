package com.elster.partners.connexo.filters.generic;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


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

        String xsrf = getTokenFromCookie(request);

        if(xsrf == null) {
            xsrf = getTokenFromAuthorizationHeader(request);
        }

        if(xsrf == null || !securityManager.verifyToken(xsrf)) {
            if(xsrf != null){
                updateToken(response, null, 0); // clear out token
            }
            redirectToLogin(request, response);
        }
        else {
            if(securityManager.needToUpdateToken()){
                updateToken(response, securityManager.getUpdatedToken(), securityManager.getMaxAge());
            }

            filterChain.doFilter(new ConnexoAuthenticationRequestWrapper(securityManager.getPrincipal(), request), response);
        }
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        String xsrf = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("X-CONNEXO-TOKEN")) {
                    xsrf = cookies[i].getValue();
                    break;
                }
            }
        }
        return xsrf;
    }

    private String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        String xsrf = null;
        String authorization = request.getHeader("Authorization");
        if(authorization != null){
            if(authorization.startsWith("Bearer ")) {
                xsrf = authorization.split(" ")[1];
            }
            else if (authorization.startsWith("Basic ")){
                ConnexoRestProxyManager restManager = ConnexoRestProxyManager.getInstance(getConnexoUrl(), authorization);
                xsrf = restManager.getToken();
            }
        }
        return xsrf;
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