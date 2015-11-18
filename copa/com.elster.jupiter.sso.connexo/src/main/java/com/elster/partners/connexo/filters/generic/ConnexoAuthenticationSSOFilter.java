package com.elster.partners.connexo.filters.generic;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Created by dragos on 11/6/2015.
 */

public class ConnexoAuthenticationSSOFilter implements Filter {
    FilterConfig filterConfig;

    // Hard-coded for now, this needs to be decoded from the token
    String user = "Dragos";
    List<String> roles = Arrays.asList("Process designer");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

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

    @Override
    public void destroy() {
    }

    private void login(final HttpServletRequest httpRequest,
                        final HttpServletResponse httpResponse) throws IOException {
        httpResponse.sendRedirect( getConnexoLoginUrl( httpRequest ) );
    }

    private String getConnexoLoginUrl(final HttpServletRequest request) {
        // TODO - get the url from a system property
        return "http://localhost:8080/apps/login/index.html";
    }
}

