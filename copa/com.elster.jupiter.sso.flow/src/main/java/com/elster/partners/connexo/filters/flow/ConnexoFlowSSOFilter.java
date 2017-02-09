package com.elster.partners.connexo.filters.flow;

import com.elster.partners.connexo.filters.flow.authorization.ConnexoAuthenticationService;
import com.elster.partners.connexo.filters.flow.identity.ConnexoFlowRestProxyService;
import com.elster.partners.connexo.filters.generic.ConnexoAbstractSSOFilter;
import com.elster.partners.connexo.filters.generic.ConnexoPrincipal;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.uberfire.backend.server.security.IOSecurityAuth;
import org.uberfire.commons.services.cdi.Veto;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Veto
public class ConnexoFlowSSOFilter extends ConnexoAbstractSSOFilter {

    private final static String LOGOUT_URI = "/logout.jsp";

    @Inject
    ConnexoFlowRestProxyService connexoFlowRestProxyService;

    @Inject
    @IOSecurityAuth
    ConnexoAuthenticationService authenticationService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = getConnexoToken(request);

        ConnexoPrincipal principal = (ConnexoPrincipal) request.getUserPrincipal();

        if (principal == null || token == null || isForbidden(request, principal)) {
            // Not authenticated; redirect to login
            redirectToLogin(request, response);
            authenticationService.setUser(null);
        } else {
            if (isLogoutRequest(request)) {
                redirectToLogout(request, response);
                authenticationService.setUser(null);
            } else {
                Set<Role> roles = new HashSet<>();
                Set<Group> groups = new HashSet<>();
                for (String role : principal.getRoles()) {
                    roles.add(new RoleImpl(role));
                }

                connexoFlowRestProxyService.getGroupsOf(principal.getName())
                        .stream()
                        .forEach(group -> groups.add(new GroupImpl(group)));
                appendKieDefaults(roles, groups);

                ConnexoUberfireSubject subject = new ConnexoUberfireSubject(principal.getName(), groups, roles);
                authenticationService.setUser(subject);
                filterChain.doFilter(new ConnexoFlowRequestWrapper(subject, request), response);
            }
        }
    }

    private void appendKieDefaults(Set<Role> roles, Set<Group> groups) {
        // Add default Administrators group, hard-coded in jBPM as Business Administrators - required to manage user tasks
        GroupImpl defaultAdministrators = new GroupImpl("Administrators");
        if (!groups.contains(defaultAdministrators)) {
            groups.add(defaultAdministrators);
        }

        // Bug in jBPM 6.4.0 web console - only the default admin role is allowed to access AdministrationPerspective
        RoleImpl defaultAdmin = new RoleImpl("admin");
        if (!roles.contains(defaultAdmin)) {
            roles.add(defaultAdmin);
        }

        // Bug in jBPM 6.4.0 rest - only the default rest-all role is allowed to access all rest resources
        RoleImpl defaultRest = new RoleImpl("rest-all");
        if (!roles.contains(defaultRest)) {
            roles.add(defaultRest);
        }
    }

    private boolean isForbidden(HttpServletRequest request, ConnexoPrincipal principal) {
        return !request.getRequestURI().startsWith("/flow/rest/") && !principal.getRoles().contains("Business process designer");
    }

    private String getConnexoToken(HttpServletRequest request) {
        String token = null;
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.split(" ")[1];
        } else {
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

    private boolean isLogoutRequest(final HttpServletRequest request) {
        return request.getRequestURI().endsWith(LOGOUT_URI);
    }
}
