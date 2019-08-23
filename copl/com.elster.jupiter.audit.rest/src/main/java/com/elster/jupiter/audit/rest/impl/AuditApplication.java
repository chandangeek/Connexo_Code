/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.rest.AuditI18N;
import com.elster.jupiter.audit.rest.AuditInfoFactory;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.validation.MessageInterpolator;
import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.audit.rest",
        service = {Application.class, TranslationKeyProvider.class},
        immediate = true,
        property = {"alias=/aud", "app=SYS", "name=" + AuditApplication.COMPONENT_NAME})
public class AuditApplication extends Application implements TranslationKeyProvider {

    public static final String COMPONENT_NAME = AuditI18N.COMPONENT_NAME;

    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile AuditService auditService;
    private volatile UserService userService;
    private volatile AuditInfoFactory auditInfoFactory;
    private volatile Clock clock;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                AuditResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setAuditService(AuditService auditService) {
        this.auditService = auditService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setAuditInfoFactory(AuditInfoFactory auditInfoFactory) {
        this.auditInfoFactory = auditInfoFactory;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(AuditDomainTranslationKeys.values()));
        translationKeys.addAll(Arrays.asList(AuditDomainContextTranslationKeys.values()));
        translationKeys.addAll(Arrays.asList(AuditOperationTranslationKeys.values()));
        return translationKeys;
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(thesaurus).to(Thesaurus.class);
            bind(thesaurus).to(MessageInterpolator.class);
            bind(userService).to(UserService.class);
            bind(transactionService).to(TransactionService.class);
            bind(auditService).to(AuditService.class);
            bind(clock).to(Clock.class);
            bind(AuditInfoFactoryImpl.class).to(AuditInfoFactoryImpl.class);
            bind(AuditLogInfoFactory.class).to(AuditLogInfoFactory.class);
            bind(auditInfoFactory).to(AuditInfoFactory.class);
        }
    }
}