/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.UtilitiesDeviceRegisteredNotification;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.energyict.mdc.sap.rest", service = {Application.class}, immediate = true,
        property = {"alias=/sap", "app=MDC", "name=" + SapApplication.COMPONENT_NAME})
public class SapApplication extends Application {
    public static final String COMPONENT_NAME = "SPR";

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile DeviceService deviceService;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification;
    private volatile Clock clock;
    private volatile TransactionService transactionService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                SapResource.class
        );
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
            bind(RegisteredNotificationEndPointInfoFactory.class).to(RegisteredNotificationEndPointInfoFactory.class);
            bind(deviceService).to(DeviceService.class);
            bind(sapCustomPropertySets).to(SAPCustomPropertySets.class);
            bind(endPointConfigurationService).to(EndPointConfigurationService.class);
            bind(nlsService).to(NlsService.class);
            bind(thesaurus).to(Thesaurus.class);
            bind(utilitiesDeviceRegisteredNotification).to(UtilitiesDeviceRegisteredNotification.class);
            bind(clock).to(Clock.class);
            bind(transactionService).to(TransactionService.class);
        }
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Reference
    public void setUtilitiesDeviceRegisteredNotification(UtilitiesDeviceRegisteredNotification utilitiesDeviceRegisteredNotification) {
        this.utilitiesDeviceRegisteredNotification = utilitiesDeviceRegisteredNotification;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
