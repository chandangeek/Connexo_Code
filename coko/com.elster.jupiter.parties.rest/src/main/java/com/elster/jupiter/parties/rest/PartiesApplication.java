package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.parties.rest" , service=Application.class , immediate = true , property = {"alias=/prt"} )
public class PartiesApplication extends Application implements ServiceLocator {

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile PartyService partyService;
    private volatile UserService userService;
    private volatile Clock clock;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(PartiesResource.class, PersonsResource.class, OrganizationsResource.class, RolesResource.class);
    }

    @Override
    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Override
    public RestQueryService getQueryService() {
        return restQueryService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    public void activate(ComponentContext context) {
        Bus.setServiceLocator(this);
    }

    public void deactivate(ComponentContext context) {
        Bus.setServiceLocator(null);
    }

    public PartyService getPartyService() {
        return partyService;
    }

    @Reference
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
