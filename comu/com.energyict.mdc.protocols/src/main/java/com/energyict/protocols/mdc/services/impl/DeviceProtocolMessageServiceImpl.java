package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
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
 * Provides an implementation for the {@link DeviceProtocolMessageService} interface
 * and registers as a OSGi component.
 * <p/>
 * Copyrights EnergyICT
 * Date: 08/11/13
 * Time: 16:08
 */
@Component(name = "com.energyict.mdc.service.deviceprotocolmessage", service = DeviceProtocolMessageService.class)
public class DeviceProtocolMessageServiceImpl implements DeviceProtocolMessageService {

    /* Services required by one of the actual protocol classes in this bundle
     * and therefore must be available in the Module provided to the guice injector. */
    private volatile Clock clock;
    private volatile MeteringService meteringService;
    private volatile IssueService issueService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private volatile PropertySpecService propertySpecService;
    private volatile TopologyService topologyService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile SocketService socketService;
    private volatile SerialComponentService serialComponentService;
    private volatile IdentificationService identificationService;
    private volatile CollectedDataFactory collectedDataFactory;
    private volatile CalendarService calendarService;
    private volatile DeviceConfigurationService deviceConfigurationService;

    private Injector injector;

    // For OSGi purpose
    public DeviceProtocolMessageServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolMessageServiceImpl(IssueService issueService, MeteringService meteringService, Clock clock, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService, PropertySpecService propertySpecService, TopologyService topologyService, SocketService socketService, SerialComponentService serialComponentService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, CalendarService calendarService, DeviceConfigurationService deviceConfigurationService, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setMeteringService(meteringService);
        this.setIssueService(issueService);
        this.setClock(clock);
        this.setJupiterPropertySpecService(jupiterPropertySpecService);
        this.setPropertySpecService(propertySpecService);
        this.setTopologyService(topologyService);
        this.setSocketService(socketService);
        this.setSerialComponentService(serialComponentService);
        this.setReadingTypeUtilService(readingTypeUtilService);
        this.setIdentificationService(identificationService);
        this.setCalendarService(calendarService);
        this.setCollectedDataFactory(collectedDataFactory);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setProtocolPluggableService(protocolPluggableService);
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
                bind(IssueService.class).toInstance(issueService);
                bind(Clock.class).toInstance(clock);
                bind(MeteringService.class).toInstance(meteringService);
                bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(SocketService.class).toInstance(socketService);
                bind(SerialComponentService.class).toInstance(serialComponentService);
                bind(TopologyService.class).toInstance(topologyService);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(IdentificationService.class).toInstance(identificationService);
                bind(CollectedDataFactory.class).toInstance(collectedDataFactory);
                bind(CalendarService.class).toInstance(calendarService);
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
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

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setJupiterPropertySpecService(com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService) {
        this.jupiterPropertySpecService = jupiterPropertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Reference
    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Reference
    public void setSerialComponentService(SerialComponentService serialComponentService) {
        this.serialComponentService = serialComponentService;
    }

    @Reference
    public void setCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactory = collectedDataFactory;
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
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