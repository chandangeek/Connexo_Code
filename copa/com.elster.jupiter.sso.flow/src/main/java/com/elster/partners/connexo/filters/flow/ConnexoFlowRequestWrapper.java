/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.flow;

import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.uberfire.commons.services.cdi.Veto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;

@Veto
public class ConnexoFlowRequestWrapper extends HttpServletRequestWrapper {

    private final User subject;
    private final HttpServletRequest realRequest;

    public ConnexoFlowRequestWrapper(User subject, HttpServletRequest request) {
        super(request);
        this.subject = subject;
        this.realRequest = request;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (subject == null || subject.getRoles().isEmpty()) {
            return this.realRequest.isUserInRole(role);
        }
        return subject.getRoles().contains(new RoleImpl(role));
    }

    @Override
    public Principal getUserPrincipal() {
        if (subject == null) {
            return realRequest.getUserPrincipal();
        }

        return new Principal() {
            @Override
            public String getName() {
                return subject.getIdentifier();
            }
        };
    }
}
