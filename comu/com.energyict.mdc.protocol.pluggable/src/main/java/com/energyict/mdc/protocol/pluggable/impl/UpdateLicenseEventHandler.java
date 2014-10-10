package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.license.License;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

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

    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile TransactionService transactionService;
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
        this.registerInboundDeviceProtocolPluggableClasses();
        this.registerDeviceProtocolPluggableClasses();
        this.registerConnectionTypePluggableClasses();
    }

    private void registerConnectionTypePluggableClasses() {
        new ConnectionTypePluggableClassRegistrar(this.protocolPluggableService, this.transactionService).
                registerAll(Collections.unmodifiableList(this.connectionTypeServices));
    }

    private void registerDeviceProtocolPluggableClasses() {
        new DeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService).
                registerAll(this.protocolPluggableService.getAllLicensedProtocols());
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        new InboundDeviceProtocolPluggableClassRegistrar(this.protocolPluggableService, this.transactionService).
                registerAll(Collections.unmodifiableList(this.inboundDeviceProtocolServices));
    }

    @Reference
    @SuppressWarnings("unused")
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = new FakeTransactionService(transactionService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.add(inboundDeviceProtocolService);
        this.registerInboundDeviceProtocolPluggableClasses();
    }

    @SuppressWarnings("unused")
    public void removeInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.remove(inboundDeviceProtocolService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.add(connectionTypeService);
        this.registerConnectionTypePluggableClasses();
    }

    @SuppressWarnings("unused")
    public void removeConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.remove(connectionTypeService);
    }

}