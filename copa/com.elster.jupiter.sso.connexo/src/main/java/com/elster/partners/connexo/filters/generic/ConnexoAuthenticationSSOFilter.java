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

        ConnexoSecurityTokenManager manager = ConnexoSecurityTokenManager.getInstance();

        Cookie xsrf = null;
        Cookie[] cookies = request.getCookies();
        for(int i=0; i<cookies.length; i++){
            if(cookies[i].getName().equals("X-CONNEXO-TOKEN")){
                xsrf = cookies[i];
                break;
            }
        }

        if(xsrf == null || !manager.verifyToken(xsrf.getValue())) {
            if(xsrf != null){
                updateToken(response, null, 0); // clear out token
            }
            redirectToLogin(request, response);
        }
        else {
            if(manager.needToUpdateToken()){
                updateToken(response, manager.getUpdatedToken(), manager.getMaxAge());
            }

            filterChain.doFilter(new ConnexoAuthenticationRequestWrapper(manager.getPrincipal(), request), response);
        }
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