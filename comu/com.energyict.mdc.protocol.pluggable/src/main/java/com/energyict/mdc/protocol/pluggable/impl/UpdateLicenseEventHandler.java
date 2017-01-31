/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.license.License;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Responds to event that is published when the MDC {@link License}
 * has been updated and will register the pluggable classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:04)
 */
@Component(name="com.energyict.mdc.protocol.pluggable.license.eventhandler", service = TopicHandler.class, immediate = true)
public class UpdateLicenseEventHandler implements TopicHandler {

    private static final String MDC_APPLICATION_KEY = "MDC";

    private volatile ServerProtocolPluggableService protocolPluggableService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile MeteringService meteringService;
    private volatile List<InboundDeviceProtocolService> inboundDeviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<ConnectionTypeService> connectionTypeServices = new CopyOnWriteArrayList<>();

    @Override
    public String getTopicMatcher() {
        return "com/elster/jupiter/license/UPDATED";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        if (MDC_APPLICATION_KEY.equals(localEvent.toOsgiEvent().getProperty("appKey"))) {
            this.registerAllPluggableClasses();
        }
    }

    private void registerAllPluggableClasses() {
        Principal principal = this.threadPrincipalService.getPrincipal();
        try {
            this.threadPrincipalService.set(getPrincipal());
            this.registerInboundDeviceProtocolPluggableClasses();
            this.registerDeviceProtocolPluggableClasses();
            this.registerConnectionTypePluggableClasses();
        } finally {
            this.threadPrincipalService.set(principal);
        }
    }

    private Principal getPrincipal() {
        return () -> "Jupiter Installer";
    }

    private void registerConnectionTypePluggableClasses() {
        new ConnectionTypePluggableClassRegistrar(this.protocolPluggableService, this.transactionService).
                registerAll(Collections.unmodifiableList(this.connectionTypeServices));
    }

    private void registerDeviceProtocolPluggableClasses() {
        new DeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService, this.meteringService).
                registerAll(this.protocolPluggableService.getAllLicensedProtocols());
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        new InboundDeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService).
                registerAll(Collections.unmodifiableList(this.inboundDeviceProtocolServices));
    }

    @Reference(name = "AProtocolPluggableService")
    @SuppressWarnings("unused")
    public void setProtocolPluggableService(ServerProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference(name = "ATransactionService")
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = new FakeTransactionService(transactionService);
    }

    @Reference(name = "AThreadPrincipalService")
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference(name = "AMeteringService")
    @SuppressWarnings("unused")
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference(name = "ZInboundDeviceProtocolService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.add(inboundDeviceProtocolService);
    }

    @SuppressWarnings("unused")
    public void removeInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.remove(inboundDeviceProtocolService);
    }

    @Reference(name = "ZConnectionTypeService", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.add(connectionTypeService);
    }

    @SuppressWarnings("unused")
    public void removeConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.remove(connectionTypeService);
    }

}