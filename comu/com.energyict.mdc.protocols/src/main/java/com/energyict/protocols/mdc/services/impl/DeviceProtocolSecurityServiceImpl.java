package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.upl.properties.PropertySpecService;

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
import java.time.Clock;

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

    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    private Injector injector;

    // For OSGi purposes
    public DeviceProtocolSecurityServiceImpl(){}

    // For testing purposes
    @Inject
    public DeviceProtocolSecurityServiceImpl(Clock clock, NlsService nlsService, PropertySpecService propertySpecService) {
        this();
        this.setClock(clock);
        this.setNlsService(nlsService);
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
                bind(Clock.class).toInstance(clock);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(DeviceProtocolSecurityService.class).toInstance(DeviceProtocolSecurityServiceImpl.this);
            }
        };
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolServiceImpl.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> securityClass = Class.forName(javaClassName);
            return this.injector.getInstance(securityClass);
        }
        catch (ConfigurationException | ProvisionException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceSecuritySupportClass(MessageSeeds.UNKNOWN_DEVICE_SECURITY_SUPPORT_CLASS, e, javaClassName);
        }
    }

}