/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.CSRFService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UsersRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    static SecurityContext securityContext;

    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    UserPreferencesService userPreferencesService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    PrivilegeThesaurus privilegeThesaurus;
    @Mock
    SecurityManagementService securityManagementService;
    @Mock
    CSRFService csrfService;

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
        UsersApplication application = new UsersApplication() {

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>();
                classes.addAll(super.getClasses());
                classes.add(SecurityRequestFilter.class);
                return classes;
            }
        };
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        when(userService.getUserPreferencesService()).thenReturn(userPreferencesService);
        application.setUserService(userService);
        application.setThreadPrincipalService(threadPrincipalService);
        application.setNlsService(nlsService);
        application.setSecurityManagementService(securityManagementService);
        application.setCSRFService(csrfService);
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(this.nlsService.getPrivilegeThesaurus()).thenReturn(this.privilegeThesaurus);
        when(transactionService.execute(any(ExceptionThrowingSupplier.class))).thenAnswer(
                invocation -> invocation.getArgumentAt(0, ExceptionThrowingSupplier.class).get());
    }

}
