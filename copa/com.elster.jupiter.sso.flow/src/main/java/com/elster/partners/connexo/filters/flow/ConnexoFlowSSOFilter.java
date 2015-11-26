package com.elster.partners.connexo.filters.flow;

import com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthorizationManager;
import com.elster.partners.connexo.filters.flow.identity.ConnexoFlowRestProxyManager;
import com.elster.partners.connexo.filters.generic.ConnexoAbstractSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;
import org.jboss.solder.core.Veto;
import org.uberfire.security.Role;
import org.uberfire.security.impl.RoleImpl;
import org.uberfire.security.server.cdi.SecurityFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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

        String token = "123456789";
        /*String token = null;
        Cookie cookies[] = request.getCookies();
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals("X-CONNEXO_TOKEN")) {
                token = cookie.getValue();
                break;
            }
        }*/

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if(principal == null || token == null){
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
        }
        else {
            if ( isLogoutRequest( request ) ) {
                redirectToLogout(request, response);
            }
            else {
                ConnexoFlowRestProxyManager manager = ConnexoFlowRestProxyManager.getInstance(getConnexoUrl(), token);

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

    private boolean isLogoutRequest( final HttpServletRequest request ) {
        return request.getRequestURI().contains( LOGOUT_URI );
    }
}
