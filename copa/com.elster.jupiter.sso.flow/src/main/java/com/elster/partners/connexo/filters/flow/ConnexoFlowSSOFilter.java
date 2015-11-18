package com.elster.partners.connexo.filters.flow;

import com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthorizationManager;
import com.elster.partners.connexo.filters.flow.identity.ConnexoRestProxyManager;
import com.elster.partners.connexo.filters.generic.ConnexoAuthenticationSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;
import org.jboss.solder.core.Veto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.security.Role;
import org.uberfire.security.impl.RoleImpl;
import org.uberfire.security.server.cdi.SecurityFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.uberfire.security.server.SecurityConstants.LOGOUT_URI;

/**
 * Created by dragos on 11/6/2015.
 */

@Veto
public class ConnexoFlowSSOFilter implements Filter {
    FilterConfig filterConfig;

    private final String CONNEXO_URL = System.getProperty("com.elster.connexo.url");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if(principal == null){
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
        }
        else {
            if ( isLogoutRequest( request ) ) {
                redirectToLogout(request, response);
            }
            else {
                String token = null;
                Cookie cookies[] = request.getCookies();
                for(Cookie cookie : cookies) {
                    if(cookie.getName().equals("X-CONNEXO_TOKEN")) {
                        token = cookie.getValue();
                        break;
                    }
                }

                ConnexoRestProxyManager.getInstance(getConnexoUrl(), token);

                List<Role> roles = new ArrayList<>();
                for(String role : principal.getRoles()) {
                    roles.add(new RoleImpl(role));
                }

                // Since the plugged in AuthorizationManager is ignored in 6.1.0 (see below), add the default admin role as a workaround
                roles.add(new RoleImpl("admin"));

                ConnexoUberfireSubject subject = new ConnexoUberfireSubject(principal.getName(), roles);

                SecurityFactory.setSubject(subject);

                // Due to a bug, this setting is ignored in 6.1.0; it has been addressed in 6.2.0
                SecurityFactory.setAuthzManager(new ConnexoAuthorizationManager());

                filterChain.doFilter(new ConnexoFlowRequestWrapper(subject, request), response);
            }
        }
    }

    @Override
    public void destroy() {
    }

    private void redirectToLogin(final HttpServletRequest httpRequest,
                                 final HttpServletResponse httpResponse) throws IOException {
        login(httpRequest, httpResponse, getConnexoLoginUrl());
    }

    private void redirectToLogout(final HttpServletRequest httpRequest,
                                 final HttpServletResponse httpResponse) throws IOException {
        login(httpRequest, httpResponse, getConnexoLogoutUrl());
    }

    private void login(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse, String url) throws IOException {
        httpResponse.sendRedirect(url);
    }

    private boolean isLogoutRequest( final HttpServletRequest request ) {
        return request.getRequestURI().contains( LOGOUT_URI );
    }

    private String getConnexoUrl() {
        return (CONNEXO_URL != null) ? CONNEXO_URL : "http://localhost:8080";
    }

    private String getConnexoLoginUrl() {
         return getConnexoUrl() + "/apps/login/index.html";
    }

    private String getConnexoLogoutUrl() {
        return getConnexoUrl() + "/apps/login/index.html?logout";
    }
}
