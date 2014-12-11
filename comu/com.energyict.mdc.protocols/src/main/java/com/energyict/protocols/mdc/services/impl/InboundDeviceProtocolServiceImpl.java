package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;

import com.energyict.protocols.mdc.InboundDeviceProtocolRule;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collection;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl implements InboundDeviceProtocolService {

    private PropertySpecService propertySpecService;
    private Injector injector;

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Activate
    public void activate() {
        Module module = this.getModule();
        this.injector = Guice.createInjector(module);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(InboundDeviceProtocolService.class).toInstance(InboundDeviceProtocolServiceImpl.this);
            }
        };
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        return this.createInboundDeviceProtocolFor(pluggableClass.getJavaClassName());
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> inboundDeviceProtocolClass = this.getClass().getClassLoader().loadClass(javaClassName);
            return (InboundDeviceProtocol) this.injector.getInstance(inboundDeviceProtocolClass);
        }
        catch (ClassNotFoundException | ConfigurationException | ProvisionException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
    }

    @Override
    public Collection<PluggableClassDefinition> getExistingInboundDeviceProtocolPluggableClasses() {
        return Arrays.asList((PluggableClassDefinition[]) InboundDeviceProtocolRule.values());
    }

}