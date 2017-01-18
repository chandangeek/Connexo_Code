package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
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

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:03
 */
@Component(name = "com.energyict.mdc.service.inbounddeviceprotocols", service = InboundDeviceProtocolService.class, immediate = true)
public class InboundDeviceProtocolServiceImpl implements InboundDeviceProtocolService {

    /* Services required by one of the actual protocol classes in this bundle
     * and therefore must be available in the Module provided to the guice injector. */
    private volatile CollectedDataFactory collectedDataFactory;
    private volatile IssueService issueService;
    private volatile IdentificationService identificationService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;

    private volatile Clock clock;
    private Injector injector;
    private Thesaurus thesaurus;

    public InboundDeviceProtocolServiceImpl() {
    }

    @Inject
    public InboundDeviceProtocolServiceImpl(MdcReadingTypeUtilService readingTypeUtilService, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService, PropertySpecService propertySpecService, Clock clock, NlsService nlsService, CollectedDataFactory collectedDataFactory, IssueService issueService, IdentificationService identificationService, MeteringService meteringService) {
        this();
        setReadingTypeUtilService(readingTypeUtilService);
        setJupiterPropertySpecService(jupiterPropertySpecService);
        setPropertySpecService(propertySpecService);
        setClock(clock);
        setNlsService(nlsService);
        setCollectedDataFactory(collectedDataFactory);
        setIssueService(issueService);
        setIdentificationService(identificationService);
        setMeteringService(meteringService);

        activate();
    }

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
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(CollectedDataFactory.class).toInstance(collectedDataFactory);
                bind(IssueService.class).toInstance(issueService);
                bind(IdentificationService.class).toInstance(identificationService);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(com.elster.jupiter.properties.PropertySpecService.class).toInstance(jupiterPropertySpecService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(Clock.class).toInstance(clock);
                bind(InboundDeviceProtocolService.class).toInstance(InboundDeviceProtocolServiceImpl.this);
            }
        };
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactory = collectedDataFactory;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
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