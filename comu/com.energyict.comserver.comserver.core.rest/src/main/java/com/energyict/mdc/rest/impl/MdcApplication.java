package com.energyict.mdc.rest.impl;

import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.rest.AutoCloseDatabaseConnection;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.rest.impl.comserver.ComPortPoolResource;
import com.energyict.mdc.rest.impl.comserver.ComPortResource;
import com.energyict.mdc.rest.impl.comserver.ComServerResource;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.rest", service = Application.class, immediate = true, property = {"alias=/mdc"})
public class MdcApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(MdcApplication.class.getSimpleName());

    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile LicensedProtocolService licensedProtocolService;
    private volatile EngineModelService engineModelService;
    private volatile PropertySpecService propertySpecService;
    private volatile TransactionService transactionService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(AutoCloseDatabaseConnection.class,
                TransactionWrapper.class,
                ExceptionLogger.class,
                ComServerResource.class,
                ComPortResource.class,
                ComPortPoolResource.class,
                DeviceCommunicationProtocolsResource.class,
                FieldResource.class,
                DeviceDiscoveryProtocolsResource.class,
                LicensedProtocolResource.class,
                TimeZoneInUseResource.class,
                UserFileReferenceResource.class,
                LoadProfileTypeResource.class,
                CodeTableResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolService = licensedProtocolService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            LOGGER.fine("Binding services using HK2");
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
            bind(licensedProtocolService).to(LicensedProtocolService.class);
            bind(propertySpecService).to(PropertySpecService.class);
            bind(engineModelService).to(EngineModelService.class);
            bind(transactionService).to(TransactionService.class);
        }
    }

}