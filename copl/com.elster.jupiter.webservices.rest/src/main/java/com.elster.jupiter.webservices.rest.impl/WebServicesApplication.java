/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.RestValidationExceptionMapper;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(name = "com.elster.jupiter.servicecall.rest",
        service = {Application.class, MessageSeedProvider.class, TranslationKeyProvider.class}, immediate = true,
        property = {"alias=/ws", "app=" + WebServicesApplication.APP_KEY, "name=" + WebServicesApplication.COMPONENT_NAME})
public class WebServicesApplication extends Application implements MessageSeedProvider, TranslationKeyProvider {

    public static final String APP_KEY = "SYS";
    public static final String COMPONENT_NAME = "WS";

    private volatile WebServicesService webServicesService;
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile EndPointConfigurationService endPointConfigurationService;


    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                RestValidationExceptionMapper.class,
                EndPointConfigurationResource.class,
                WebServicesResource.class,
                WebServicesFieldResource.class
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
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST)
                .join(nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
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
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(EndPointConfigurationInfoFactory.class).to(EndPointConfigurationInfoFactory.class);
            bind(WebServicesInfoFactory.class).to(WebServicesInfoFactory.class);
            bind(EndpointConfigurationLogInfoFactory.class).to(EndpointConfigurationLogInfoFactory.class);
            bind(webServicesService).to(WebServicesService.class);
            bind(transactionService).to(TransactionService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(endPointConfigurationService).to(EndPointConfigurationService.class);
            bind(userService).to(UserService.class);
        }
    }
}