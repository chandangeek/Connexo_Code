package com.elster.partners.connexo.filters.facts;

import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


/**
 * Created by dragos on 11/11/2015.
 */
public class ConnexoFactsSSOFilter implements Filter {
    FilterConfig filterConfig;

    private final String CONNEXO_URL = System.getProperty("com.elster.connexo.url");

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getRequestURI().startsWith(request.getContextPath() + "/services") || request.getRequestURI().startsWith(request.getContextPath() + "/JsAPI")) {
            filterChain.doFilter(servletRequest, servletResponse); // Just continue chain.
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
                    doLogin(principal, request, response);
                } else {
                    filterChain.doFilter(request, response);
                }
            }
        }
    }

    @Override
    public void destroy() {

    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = (CONNEXO_URL != null) ? CONNEXO_URL : "http://localhost:8080";
        response.sendRedirect(url + "/apps/login/index.html");
    }

    private void redirectToLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = (CONNEXO_URL != null) ? CONNEXO_URL : "http://localhost:8080";
        response.sendRedirect(url + "/apps/login/index.html?logout");
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

    private boolean isLoggedIn(HttpServletRequest request) {
        return (request.getSession().getAttribute("SessionData") != null);
    }

    private void doLogin(ConnexoPrincipal principal, HttpServletRequest request, HttpServletResponse response) throws IOException {

        ConnexoFactsWebServiceManager manager = new ConnexoFactsWebServiceManager(request.getServerPort(), request.getContextPath(), request.getProtocol());

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
