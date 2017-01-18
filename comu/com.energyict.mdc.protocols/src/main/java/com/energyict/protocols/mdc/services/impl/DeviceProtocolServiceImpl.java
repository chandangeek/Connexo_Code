package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.DeviceProtocolDialectName;
import com.energyict.protocolimplv2.abnt.AbntTranslationKeys;
import com.energyict.protocolimplv2.ace4000.ACE4000Properties;
import com.energyict.protocolimplv2.common.CommonV2TranslationKeys;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.dlms.DlmsTranslationKeys;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155TranslationKeys;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.AbstractSMSServletBasedInboundDeviceProtocol;
import com.energyict.protocolimplv2.elster.ctr.MTU155.discover.CtrInboundDeviceProtocol;
import com.energyict.protocolimplv2.elster.garnet.GarnetTranslationKeys;
import com.energyict.protocolimplv2.security.SecurityPropertySpecName;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.ip.IpMessageSeeds;
import com.energyict.protocols.mdc.protocoltasks.CTRTranslationKeys;
import com.energyict.protocols.mdc.protocoltasks.EiWebPlusDialectProperties;
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
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link DeviceProtocolService} interface
 * and registers as a OSGi component.
 * <p>
 * Copyrights EnergyICT
 * Date: 06/11/13
 * Time: 11:03
 */
@Component(name = "com.energyict.mdc.service.deviceprotocols",
        service = {DeviceProtocolService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true,
        property = "name=" + DeviceProtocolService.COMPONENT_NAME)
public class DeviceProtocolServiceImpl implements DeviceProtocolService, MessageSeedProvider, TranslationKeyProvider {

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
    private volatile DeviceMessageFileService deviceMessageFileService;

    private Injector injector;
    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;
    private volatile OrmClient ormClient;

    // For OSGi purposes
    public DeviceProtocolServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceProtocolServiceImpl(IssueService issueService, MeteringService meteringService, Clock clock, OrmService ormService, NlsService nlsService, com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService, PropertySpecService propertySpecService, TopologyService topologyService, SocketService socketService, SerialComponentService serialComponentService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, CalendarService calendarService, DeviceConfigurationService deviceConfigurationService, DeviceMessageFileService deviceMessageFileService, TransactionService transactionService, ProtocolPluggableService protocolPluggableService) {
        this();
        setMeteringService(meteringService);
        setTransactionService(transactionService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setClock(clock);
        setJupiterPropertySpecService(jupiterPropertySpecService);
        setPropertySpecService(propertySpecService);
        setTopologyService(topologyService);
        setSocketService(socketService);
        setSerialComponentService(serialComponentService);
        setReadingTypeUtilService(readingTypeUtilService);
        setIdentificationService(identificationService);
        setCollectedDataFactory(collectedDataFactory);
        setCalendarService(calendarService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceMessageFileService(deviceMessageFileService);
        setProtocolPluggableService(protocolPluggableService);
        activate();
    }

    @Activate
    public void activate() {
        Module module = this.getModule();
        this.dataModel.register(module);
        this.injector = Guice.createInjector(module);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(OrmClient.class).toInstance(ormClient);
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
                bind(DeviceMessageFileService.class).toInstance(deviceMessageFileService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DeviceProtocolService.class).toInstance(DeviceProtocolServiceImpl.this);
            }
        };
    }

    @Override
    public Object createProtocol(String className) {
        try {
            // Attempt to load the class to verify that this class is managed by this bundle
            Class<?> protocolClass = this.getClass().getClassLoader().loadClass(className);
            return this.injector.getInstance(protocolClass);
        }
        catch (ClassNotFoundException | ConfigurationException | ProvisionException e) {
            throw new ProtocolCreationException(MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, className);
        }
    }

    public IssueService getIssueService() {
        return issueService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
        this.createOrmClientIfAllDependenciesHaveBeenResolved();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceProtocolService.COMPONENT_NAME, "DeviceProtocol service 1");
        this.createOrmClientIfAllDependenciesHaveBeenResolved();
    }

    private void createOrmClientIfAllDependenciesHaveBeenResolved() {
        if (this.transactionService != null && this.dataModel != null) {
            this.setOrmClient(new OrmClientImpl(this.dataModel, this.transactionService, clock));
        }
    }

    private void setOrmClient(OrmClient ormClient) {
        this.ormClient = ormClient;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceProtocolService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
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
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        // Just making sure that this bundle activates after the bundle that provides connections (see com.energyict.mdc.protocol.api.ConnectionProvider)
    }

    @Reference
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceMessageFileService(DeviceMessageFileService deviceMessageFileService) {
        this.deviceMessageFileService = deviceMessageFileService;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Stream.of(
                Arrays.stream(IpMessageSeeds.values()),
                Arrays.stream(MessageSeeds.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(CtrInboundDeviceProtocol.TranslationKeys.values()),
                Arrays.stream(EiWebPlusDialectProperties.TranslationKeys.values()),
                Arrays.stream(SecurityPropertySpecName.values()),
                Arrays.stream(DeviceProtocolDialectName.values()),
                Arrays.stream(DlmsProperties.TranslationKeys.values()),
                Arrays.stream(AbstractSMSServletBasedInboundDeviceProtocol.TranslationKeys.values()),
                Arrays.stream(CustomPropertySetTranslationKeys.values()),
                Arrays.stream(AbntTranslationKeys.values()),
                Arrays.stream(CTRTranslationKeys .values()),
                Arrays.stream(GarnetTranslationKeys.values()),
                Arrays.stream(CommonV2TranslationKeys.values()),
                Arrays.stream(DlmsTranslationKeys.values()),
                Arrays.stream(MTU155TranslationKeys.values()),
                Arrays.stream(ACE4000Properties.TranslationKeys.values()),
                Arrays.stream(com.energyict.protocols.mdc.services.impl.TranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

}