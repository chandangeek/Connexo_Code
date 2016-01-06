package com.elster.partners.connexo.filters.flow;

import com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthorizationManager;
import com.elster.partners.connexo.filters.flow.identity.ConnexoFlowRestProxyManager;
import com.elster.partners.connexo.filters.generic.ConnexoAbstractSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoAuthenticationRequestWrapper;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;
import org.jboss.solder.core.Veto;
import org.uberfire.security.Role;
import org.uberfire.security.impl.RoleImpl;
import org.uberfire.security.server.cdi.SecurityFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class ConnexoFlowSSOFilter extends ConnexoAbstractSSOFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = getConnexoToken(request);

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if(principal == null || token == null || isForbidden(request, principal)){
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
        }
        else {
            if ( isLogoutRequest( request ) ) {
                redirectToLogout(request, response);
            }
            else {
                List<Role> roles = new ArrayList<>();
                for(String role : principal.getRoles()) {
                    roles.add(new RoleImpl(role));
                }

                ConnexoUberfireSubject subject = new ConnexoUberfireSubject(principal.getName(), roles);

                SecurityFactory.setSubject(subject);
                SecurityFactory.setAuthzManager(new ConnexoAuthorizationManager());

                filterChain.doFilter(new ConnexoFlowRequestWrapper(subject, request), response);
            }
        }
    }

    private boolean isForbidden(HttpServletRequest request, ConnexoPrincipal principal) {
        return !request.getRequestURI().startsWith("/flow/rest/") && !principal.getRoles().contains("Business process designer");
    }

    private String getConnexoToken(HttpServletRequest request) {
        String token = null;
        String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")){
            token = authorization.split(" ")[1];
        }
        else {
            Cookie cookies[] = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("X-CONNEXO-TOKEN")) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        return token;
    }

    private boolean isLogoutRequest( final HttpServletRequest request ) {
        return request.getRequestURI().contains( LOGOUT_URI );
    }
}
