package com.elster.jupiter.users.rest.impl;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.users.rest" , service=Application.class , immediate = true , property = {"alias=/usr", "app=SYS", "name=" + UsersApplication.COMPONENT_NAME} )
public class UsersApplication extends Application implements BinderProvider {
    public static final String COMPONENT_NAME = "USR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile UserPreferencesService userPreferencesService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(GroupResource.class,
                               UserResource.class,
                               PrivilegeResource.class,
                               DomainResource.class,
                               ResourceResource.class,
                               CurrentUserResource.class,
                               UsersFieldResource.class);
    }
    
    @Reference
    public void setUserService(UserService partyService) {
        this.userService = partyService;
    }
    
    @Reference
    public void setUserPreferencesService(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(userService).to(UserService.class);
                bind(userPreferencesService).to(UserPreferencesService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
                bind(threadPrincipalService).to(ThreadPrincipalService.class);
            }
        };
    }
}
