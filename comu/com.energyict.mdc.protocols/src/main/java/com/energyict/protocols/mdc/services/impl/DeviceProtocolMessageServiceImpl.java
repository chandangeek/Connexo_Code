package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.IdentificationService;

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
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class)
public class DeviceProtocolMessageServiceImpl implements DeviceProtocolMessageService {

    private volatile TopologyService topologyService;
    private volatile IdentificationService identificationService;

    private Injector injector;

    // For OSGi purpose
    public DeviceProtocolMessageServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolMessageServiceImpl(TopologyService topologyService, IdentificationService identificationService) {
        this();
        this.setTopologyService(topologyService);
        this.setIdentificationService(identificationService);
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
                bind(TopologyService.class).toInstance(topologyService);
                bind(IdentificationService.class).toInstance(identificationService);
                bind(DeviceProtocolMessageService.class).toInstance(DeviceProtocolMessageServiceImpl.this);
            }
        };
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String javaClassName) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> messageConverterClass = Class.forName(javaClassName);
            return this.injector.getInstance(messageConverterClass);
        }
        catch (ConfigurationException | ProvisionException e) {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, e, javaClassName);
        }
        catch (ClassNotFoundException e) {
            throw DeviceProtocolAdapterCodingExceptions.unKnownDeviceMessageConverterClass(MessageSeeds.UNKNOWN_DEVICE_MESSAGE_CONVERTER_CLASS, e, javaClassName);
        }
    }

}