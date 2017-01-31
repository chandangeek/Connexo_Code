/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.UserInfoFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.users.rest" , service=Application.class , immediate = true , property = {"alias=/usr", "app=SYS", "name=" + UsersApplication.COMPONENT_NAME} )
public class UsersApplication extends Application {
    public static final String COMPONENT_NAME = "USR";

    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;
    private volatile UserService userService;
    private volatile UserPreferencesService userPreferencesService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(GroupResource.class,
                UserDirectoryResource.class,
                UserResource.class,
                PrivilegeResource.class,
                DomainResource.class,
                ResourceResource.class,
                CurrentUserResource.class,
                UsersFieldResource.class,
                FindGroupResource.class,
                FindUserResource.class,
                WorkGroupResource.class);
    }
    
    @Reference
    public void setUserService(UserService partyService) {
        this.userService = partyService;
        this.userPreferencesService = partyService.getUserPreferencesService();
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

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> sglt = new HashSet<>();
        sglt.addAll(super.getSingletons());
        sglt.add(new HK2Binder());
        return sglt;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(userService).to(UserService.class);
            bind(userPreferencesService).to(UserPreferencesService.class);
            bind(transactionService).to(TransactionService.class);
            bind(restQueryService).to(RestQueryService.class);
            bind(threadPrincipalService).to(ThreadPrincipalService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(GroupInfoFactory.class).to(GroupInfoFactory.class);
            bind(UserInfoFactoryImpl.class).to(UserInfoFactory.class);
        }
    }
}
