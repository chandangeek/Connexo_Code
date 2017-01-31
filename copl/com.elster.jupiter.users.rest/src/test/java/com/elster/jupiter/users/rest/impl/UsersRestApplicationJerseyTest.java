/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.nls.PrivilegeThesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;

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
        return application;
    }

    @Override
    public void setupMocks() {
        super.setupMocks();
        when(this.nlsService.getPrivilegeThesaurus()).thenReturn(this.privilegeThesaurus);
        when(transactionService.execute(any(Transaction.class))).thenAnswer(
                invocation -> ((Transaction)invocation.getArguments()[0]).perform());
    }

}