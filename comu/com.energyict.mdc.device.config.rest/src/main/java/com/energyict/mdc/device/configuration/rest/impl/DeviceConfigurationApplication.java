package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.AutoCloseDatabaseConnection;
import com.energyict.mdc.common.rest.ExceptionLogger;
import com.energyict.mdc.common.rest.TransactionWrapper;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Component(name = "com.energyict.dtc.rest", service = Application.class, immediate = true, property = {"alias=/dtc"})
public class DeviceConfigurationApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(DeviceConfigurationApplication.class.getSimpleName());

    private DeviceConfigurationService deviceConfigurationService;
    private ProtocolPluggableService protocolPluggableService;

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(
                AutoCloseDatabaseConnection.class,
                TransactionWrapper.class,
                ExceptionLogger.class,
                DeviceTypeResource.class,
                DeviceProtocolResource.class
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
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    class HK2Binder extends AbstractBinder {

        @Override
        protected void configure() {
            LOGGER.fine("Binding services using HK2");
            bind(deviceConfigurationService).to(DeviceConfigurationService.class);
            bind(protocolPluggableService).to(ProtocolPluggableService.class);
        }
    }

}