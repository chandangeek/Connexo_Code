package com.elster.partners.connexo.filters.facts;

import com.elster.partners.connexo.filters.generic.ConnexoAbstractSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


/**
 * Created by dragos on 11/11/2015.
 */
public class ConnexoFactsSSOFilter extends ConnexoAbstractSSOFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (shouldExcludUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if(principal == null){
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
        }
        else {
            if (isLogoutRequest(request)) {
                redirectToLogout(request, response);
            } else {
                if (!isLoggedIn(request) && !isLoginRequest(request)) {
                    // TODO : if the response is license expired, and the user is admin, redirect to the default login page
                    authenticate(principal, request, response);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        return (request.getSession().getAttribute("SessionData") != null);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        if(request.getRequestURI().contains("/logoff.i4")) {
            return true;
        }

        return false;
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        if( request.getRequestURI().startsWith(request.getContextPath() + "/logon.i4") && request.getParameter("LoginWebserviceId") != null) {
            return true;
        }

        return false;
    }

    private void authenticate(ConnexoPrincipal principal, HttpServletRequest request, HttpServletResponse response) throws IOException {

        ConnexoFactsWebServiceManager manager = new ConnexoFactsWebServiceManager(this.properties, request.getServerPort(), request.getContextPath(), request.getProtocol());

        Optional<String> result = manager.getUser(principal.getName());
        if (!result.isPresent() || !result.get().equals("SUCCESS")) {
            result = manager.createUser(principal.getName());
        }

        if (result.isPresent() && result.get().equals("SUCCESS")) {
            result = manager.login(principal.getName());

            if (result.isPresent()) {
                response.sendRedirect(request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI())) + request.getContextPath() + "/logon.i4?LoginWebserviceId=" +result.get()+"&disablelogoff=true");
            }
        }
    }

}
