package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.users.rest" , service=Application.class , immediate = true , property = {"alias=/usr", "app=SYS", "name=" + UsersApplication.COMPONENT_NAME} )
public class UsersApplication extends Application implements BinderProvider {
    public static final String COMPONENT_NAME = "USR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(GroupResource.class, UserResource.class, PrivilegeResource.class, DomainResource.class, ResourceResource.class);
    }

    @Reference
    public void setUserService(UserService partyService) {
        this.userService = partyService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(userService).to(UserService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }
}
