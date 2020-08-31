/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.facts;

import com.elster.partners.connexo.filters.generic.ConnexoAbstractSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ConnexoFactsSSOFilter extends ConnexoAbstractSSOFilter {
    private final String USER_REPORTS_VIEWER = "reportsViewer";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
            IOException,
            ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (shouldExcludUrl(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if (principal == null || isForbidden(request, principal)) {
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
        } else {
            if (isLogoutRequest(request)) {
                clearSession(request);
                redirectToLogout(request, response);
            } else {
                if (!isLoggedIn(request) && !isLoginRequest(request)) {
                    authenticate(principal, request, response);
                } else {
                    // When navigating to root, Yellowfin always assumes logon, so we need to get around that
                    if (isRootRequest(request)) {
                        redirectToEntry(request, response);
                    } else {
                        filterChain.doFilter(request, response);
                    }
                }
            }
        }
    }

    private boolean isForbidden(HttpServletRequest request, ConnexoPrincipal principal) {
        return !request.getRequestURI().startsWith("/facts/services/") && !request.getRequestURI()
                .equals("/facts/JsAPI") && !(principal.getPrivileges()
                .contains("privilege.design.reports") || principal.getPrivileges().contains("privilege.administrate.reports"));
    }

    private void redirectToEntry(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getRequestURL() + "logon.i4");
    }

    private boolean isRootRequest(HttpServletRequest request) {
        return request.getRequestURI().replace('/', ' ').trim().equals("facts");
    }

    private void clearSession(HttpServletRequest request) {
        request.getSession().setAttribute("SessionData", null);
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        return (request.getSession().getAttribute("SessionData") != null);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        if (request.getRequestURI().contains("/logoff.i4")) {
            return true;
        }

        return false;
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        if (request.getRequestURI()
                .startsWith(request.getContextPath() + "/logon.i4") && request.getParameter("LoginWebserviceId") != null) {
            return true;
        }

        return false;
    }

    private void authenticate(ConnexoPrincipal principal, HttpServletRequest request, HttpServletResponse response) throws
            IOException {

        ConnexoFactsWebServiceManager manager = new ConnexoFactsWebServiceManager(request.getLocalPort(), request
                .getContextPath(), request.getProtocol());

        String userName = principal.getName();
        List<String> privileges = principal.getPrivileges();
        if (principal.getPrivileges().contains("privilege.view.reports") &&
                !(principal.getPrivileges().contains("privilege.design.reports") || principal.getPrivileges().contains("privilege.administrate.reports"))) {
            userName = USER_REPORTS_VIEWER;
            privileges = Collections.singletonList("privilege.view.reports");
        }
        Optional<String> result = manager.getUser(userName, privileges);
        if (!result.isPresent() || !result.get().equals("SUCCESS")) {
            result = manager.createUser(userName, privileges);
        }

        if (result.isPresent() && result.get().equals("SUCCESS")) {
            result = manager.login(userName);

            if (result.isPresent()) {
                response.sendRedirect(request.getRequestURL()
                        .substring(0, request.getRequestURL()
                                .indexOf(request.getRequestURI())) + request.getContextPath() + "/logon.i4?LoginWebserviceId=" + result
                        .get());
            }
        }
    }

}
