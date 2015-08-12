package com.elster.jupiter.users.rest.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.mockito.Mock;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

public class UsersRestApplicationJerseyTest extends FelixRestApplicationJerseyTest {
    
    @Mock
    RestQueryService restQueryService;
    @Mock
    UserService userService;
    @Mock
    UserPreferencesService userPreferencesService;
    @Mock
    ThreadPrincipalService threadPrincipalService;
    @Mock
    static SecurityContext securityContext;
    @Mock
    NlsService nlsService;
    @Mock
    Thesaurus thesaurus;
    
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
            
            @Override
            public Set<Object> getSingletons() {
                Set<Object> hashSet = new HashSet<>();
                hashSet.addAll(super.getSingletons());
                hashSet.add(this.getBinder());
                return Collections.unmodifiableSet(hashSet);
            }
        };
        application.setRestQueryService(restQueryService);
        application.setTransactionService(transactionService);
        when(userService.getUserPreferencesService()).thenReturn(userPreferencesService);
        application.setUserService(userService);
        application.setThreadPrincipalService(threadPrincipalService);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        application.setNlsService(nlsService);
        return application;
    }

    @Override
    protected MessageSeed[] getMessageSeeds() {
        return new MessageSeed[0];
    }
}
