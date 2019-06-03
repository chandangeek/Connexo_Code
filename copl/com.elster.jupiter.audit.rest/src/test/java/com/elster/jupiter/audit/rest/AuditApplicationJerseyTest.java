/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.impl.AuditApplication;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.UserService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AuditApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    static SecurityContext securityContext;
    @Mock
    AuditService auditService;
    @Mock
    RestQueryService restQueryService;
    @Mock
    Clock clock;
    @Mock
    UserService userService;
    @Mock
    AuditInfoFactory auditInfoFactory;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);
        AuditApplication app = new AuditApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        app.setClock(clock);
        app.setTransactionService(transactionService);
        app.setNlsService(nlsService);
        app.setAuditService(auditService);
        app.setUserService(userService);
        app.setAuditInfoFactory(auditInfoFactory);
        return app;
    }
}