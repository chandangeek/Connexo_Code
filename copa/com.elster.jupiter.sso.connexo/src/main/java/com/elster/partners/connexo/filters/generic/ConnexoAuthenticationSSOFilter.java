package com.elster.partners.connexo.filters.generic;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoAuthenticationSSOFilter extends ConnexoAbstractSSOFilter {

    // Hard-coded for now, this needs to be decoded from the token
    String user = "TestUser";
    List<String> roles = Arrays.asList("Process designer");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (shouldExcludUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // TODO - add token check
        //if(check the token here) {
            // TODO - Not authenticated - redirect to login page
        //}
        //else {
            // TODO - get the user name, roles and other properties from the token
            ConnexoPrincipal principal = new ConnexoPrincipal(user, roles);

            filterChain.doFilter(new ConnexoAuthenticationRequestWrapper(principal, request), response);
        //}
    }
}