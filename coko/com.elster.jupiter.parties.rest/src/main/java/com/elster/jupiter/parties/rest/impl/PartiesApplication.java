package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfoFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(
        name = "com.elster.jupiter.parties.rest",
        service = Application.class,
        immediate = true,
        property = {"alias=/prt", "app=SYS", "name=" + PartiesApplication.COMPONENT_NAME})
public class PartiesApplication extends Application {

    public static final String COMPONENT_NAME = "PRT";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile PartyService partyService;
    private volatile UserService userService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile UserInfoFactory userInfoFactory;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                PartiesResource.class,
                PersonsResource.class,
                OrganizationsResource.class,
                RolesResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    public PartyService getPartyService() {
        return partyService;
    }

    @Reference
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService){
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setUserInfoFactory(UserInfoFactory userInfoFactory) {
        this.userInfoFactory = userInfoFactory;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(partyService).to(PartyService.class);
            bind(userService).to(UserService.class);
            bind(clock).to(Clock.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(Fetcher.class).to(Fetcher.class);
            bind(userInfoFactory).to(UserInfoFactory.class);
        }
    }
}
