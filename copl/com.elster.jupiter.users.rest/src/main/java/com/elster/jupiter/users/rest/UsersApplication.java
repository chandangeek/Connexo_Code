package com.elster.jupiter.users.rest;

import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.users.rest" , service=Application.class , immediate = true , property = {"alias=/usr"} )
public class UsersApplication extends Application implements ServiceLocator {

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(GroupResource.class, UserResource.class, PrivilegeResource.class);
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public void setUserService(UserService partyService) {
        this.userService = partyService;
    }

    public RestQueryService getRestQueryService() {
        return restQueryService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
    }

    public void deactivate(ComponentContext context) {
        Bus.setServiceLocator(null);
    }


}
