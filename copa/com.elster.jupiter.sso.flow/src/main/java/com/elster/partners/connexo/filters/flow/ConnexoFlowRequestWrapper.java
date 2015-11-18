package com.elster.partners.connexo.filters.flow;

import org.jboss.solder.core.Veto;
import org.uberfire.security.Subject;
import org.uberfire.security.impl.RoleImpl;

import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Veto
public class ConnexoFlowRequestWrapper extends HttpServletRequestWrapper {

    Subject subject;
    HttpServletRequest realRequest;

    public ConnexoFlowRequestWrapper(Subject subject, HttpServletRequest request) {
        super(request);
        this.subject = subject;
        this.realRequest = request;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (subject == null || subject.getRoles().isEmpty()) {
            return this.realRequest.isUserInRole(role);
        }
        return subject.hasRole(new RoleImpl(role));
    }

    @Override
    public Principal getUserPrincipal() {
        if (subject == null) {
            return realRequest.getUserPrincipal();
        }

        return new Principal() {
            @Override
            public String getName() {
                return subject.getName();
            }
        };
    }
}
