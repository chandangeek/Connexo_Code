/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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

@Component(name = "com.energyict.mdc.service.deviceprotocolsecurity", service = DeviceProtocolSecurityService.class)
public class DeviceProtocolSecurityServiceImpl implements DeviceProtocolSecurityService {

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

    private volatile Thesaurus thesaurus;
    private Injector injector;

    // For OSGi purposes
    public DeviceProtocolSecurityServiceImpl(){}

    // For testing purposes
    @Inject
    public DeviceProtocolSecurityServiceImpl(IssueService issueService, MeteringService meteringService, Clock clock,
                                             com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService,
                                             PropertySpecService propertySpecService, TopologyService topologyService,
                                             SocketService socketService, SerialComponentService serialComponentService,
                                             MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService,
                                             CollectedDataFactory collectedDataFactory, CalendarService calendarService,
                                             DeviceConfigurationService deviceConfigurationService, ProtocolPluggableService protocolPluggableService,
                                             NlsService nlsService) {
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
        this.setCollectedDataFactory(collectedDataFactory);
        this.setCalendarService(calendarService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setNlsService(nlsService);
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
                bind(DeviceProtocolSecurityService.class).toInstance(DeviceProtocolSecurityServiceImpl.this);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        };
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolServiceImpl.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
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
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
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