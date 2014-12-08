package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Provides an implementation for the {@link DeviceProtocolSecurityService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:05
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolsecurity", service = DeviceProtocolSecurityService.class)
public class DeviceProtocolSecurityServiceImpl implements DeviceProtocolSecurityService {

    private volatile PropertySpecService propertySpecService;

    private Injector injector;

    // For OSGi purposes
    public DeviceProtocolSecurityServiceImpl(){}

    // For testing purposes
    @Inject
    public DeviceProtocolSecurityServiceImpl(PropertySpecService propertySpecService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.activate();
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
                bind(DeviceProtocolSecurityService.class).toInstance(DeviceProtocolSecurityServiceImpl.this);
            }
        };
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> securityClass = Class.forName(javaClassName);
            Object object = this.injector.getInstance(securityClass);
            if (object instanceof DeviceProtocolSecurityCapabilities) {
                DeviceProtocolSecurityCapabilities securityCapabilities = (DeviceProtocolSecurityCapabilities) object;
                securityCapabilities.setPropertySpecService(this.propertySpecService);
                return securityCapabilities;
            }
            else {
                return object;
            }
        }
        catch (ConfigurationException | ProvisionException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(MessageSeeds.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS, e, javaClassName);
        }
    }

}