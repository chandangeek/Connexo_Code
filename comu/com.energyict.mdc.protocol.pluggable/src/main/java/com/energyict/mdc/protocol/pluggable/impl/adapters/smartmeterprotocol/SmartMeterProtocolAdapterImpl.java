package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannelInputStreamAdapter;
import com.energyict.mdc.io.ComChannelOutputStreamAdapter;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.LegacyMessageConverter;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SmartMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AdapterDeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolTopologyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLOfflineDeviceAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link SmartMeterProtocolAdapter} interface.
 *
 * @author gna
 * @since 5/04/12 - 13:13
 */
public class SmartMeterProtocolAdapterImpl extends DeviceProtocolAdapterImpl implements DeviceProtocol, SmartMeterProtocolAdapter {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working.
     */
    private final SmartMeterProtocol meterProtocol;

    /**
     * The use <code>IssueService</code> which can be used for this adapter.
     */
    private final IssueService issueService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final MessageAdapterMappingFactory messageAdapterMappingFactory;
    private final CollectedDataFactory collectedDataFactory;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final IdentificationService identificationService;

    /**
     * The DeviceSecuritySupport component that <i>can</i> be used during communication.
     */
    private DeviceSecuritySupport deviceSecuritySupport;

    /**
     * The DeviceMessageSupport component that <i>can</i> be used during communication.
     */
    private DeviceMessageSupport deviceMessageSupport;

    /**
     * The offline device for which this adapter is working.
     */
    private OfflineDevice offlineDevice;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceClockSupport} functionality
     */
    private SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport} functionality
     */
    private SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceMessageSupport} functionality
     */
    private SmartMeterProtocolMessageAdapter smartMeterProtocolMessageAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceTopologySupport} functionality
     */
    private DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceLogBookSupport} functionality
     */
    private SmartMeterProtocolLogBookAdapter smartMeterProtocolLogBookAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport} functionality
     */
    private SmartMeterProtocolRegisterAdapter smartMeterProtocolRegisterAdapter;

    /**
     * The adapter used for the {@link DeviceSecuritySupport} functionality.
     */
    private SmartMeterProtocolSecuritySupportAdapter smartMeterProtocolSecuritySupportAdapter;

    /**
     * The logger used by the protocol.
     */
    private Logger protocolLogger;

    /**
     * The used HHUEnabler.
     */
    private HHUEnabler hhuEnabler;

    /**
     * The adapter used for the property handling.
     */
    private PropertiesAdapter propertiesAdapter;

    public SmartMeterProtocolAdapterImpl(SmartMeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, DataModel dataModel, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, IdentificationService identificationService, Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(propertySpecService, protocolPluggableService, thesaurus, securitySupportAdapterMappingFactory, dataModel, capabilityAdapterMappingFactory);
        this.messageAdapterMappingFactory = messageAdapterMappingFactory;
        this.meteringService = meteringService;
        this.identificationService = identificationService;
        this.thesaurus = thesaurus;
        this.protocolLogger = Logger.getAnonymousLogger(); // default for now
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        initializeAdapters();
        initInheritors();
    }

    /**
     * Initializes the inheritance classes.
     */
    private void initInheritors() {
        if (this.meterProtocol instanceof HHUEnabler) {
            this.hhuEnabler = (HHUEnabler) this.meterProtocol;
        }
    }

    /**
     * Initializes the adapters so they can be used in the {@link DeviceProtocol} calls.
     */
    protected void initializeAdapters() {
        this.propertiesAdapter = new PropertiesAdapter();
        this.smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(getSmartMeterProtocol());
        this.smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory, identificationService);
        this.deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter(issueService, collectedDataFactory);
        this.smartMeterProtocolLogBookAdapter = new SmartMeterProtocolLogBookAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory, meteringService);
        this.smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory);

        if (!DeviceMessageSupport.class.isAssignableFrom(getProtocolClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.smartMeterProtocolMessageAdapter = new SmartMeterProtocolMessageAdapter(getSmartMeterProtocol(), this.messageAdapterMappingFactory, this.getProtocolPluggableService(), issueService, this.collectedDataFactory, this.deviceMessageSpecificationService);
        } else {
            this.deviceMessageSupport = (DeviceMessageSupport) this.meterProtocol;
        }

        if (!com.energyict.mdc.upl.security.DeviceSecuritySupport.class.isAssignableFrom(getProtocolClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.smartMeterProtocolSecuritySupportAdapter =
                    new SmartMeterProtocolSecuritySupportAdapter(
                            getSmartMeterProtocol(),
                            this.getPropertySpecService(),
                            this.getProtocolPluggableService(),
                            this.propertiesAdapter,
                            this.getSecuritySupportAdapterMappingFactory());
        } else {
            this.deviceSecuritySupport = (DeviceSecuritySupport) this.meterProtocol;
        }
    }

    protected Class getProtocolClass() {
        if (meterProtocol instanceof UPLProtocolAdapter) {
            return ((UPLProtocolAdapter) meterProtocol).getActualClass();
        } else {
            return meterProtocol.getClass();
        }
    }

    @Override
    public void init(final OfflineDevice offlineDevice, com.energyict.mdc.protocol.ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        doInit(comChannel);
    }

    @Override
    public void init(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, com.energyict.mdc.protocol.ComChannel comChannel) {
        this.offlineDevice = new UPLOfflineDeviceAdapter(offlineDevice);
        doInit(comChannel);
    }

    private void doInit(com.energyict.mdc.protocol.ComChannel comChannel) {
        try {
            this.meterProtocol.init(
                    new ComChannelInputStreamAdapter(comChannel),
                    new ComChannelOutputStreamAdapter(comChannel),
                    this.getDeviceTimeZoneFromProperties(),
                    this.protocolLogger);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        this.propertiesAdapter.copyProperties(comChannel.getProperties());
    }

    @Override
    public LegacyMessageConverter getLegacyMessageConverter() {
        return this.smartMeterProtocolMessageAdapter.getLegacyMessageConverter();
    }

    private TimeZone getDeviceTimeZoneFromProperties() {
        TimeZone timeZone = this.propertiesAdapter.getProperties().getTypedProperty(DeviceProtocolProperty.DEVICE_TIME_ZONE.javaFieldName());
        if (timeZone == null) {
            return TimeZone.getDefault();
        } else {
            return timeZone;
        }
    }

    @Override
    public void terminate() {
        try {
            this.meterProtocol.release();
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    @Override
    public String getProtocolDescription() {
        return this.meterProtocol.getProtocolDescription();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        //todo
        return DeviceFunction.METER;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        List<PropertySpec> adapterOptionalPropertySpecs = this.getAdapterOptionalProperties();
        Set<String> adapterOptionalPropertyNames =
                adapterOptionalPropertySpecs
                        .stream()
                        .map(PropertySpec::getName)
                        .collect(Collectors.toSet());
        propertySpecs.addAll(adapterOptionalPropertySpecs);
        this.meterProtocol
                .getRequiredProperties()
                .stream()
                .filter(spec -> !adapterOptionalPropertyNames.contains(spec.getName()))
                .forEach(propertySpecs::add);
        this.meterProtocol
                .getOptionalProperties()
                .stream()
                .filter(spec -> !adapterOptionalPropertyNames.contains(spec.getName()))
                .forEach(propertySpecs::add);
        return propertySpecs;
    }

    @Override
    public String getSerialNumber() {
        try {
            return this.meterProtocol.getMeterSerialNumber();
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        return this.smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(final List<LoadProfileReader> loadProfiles) {
        return this.smartMeterProtocolLoadProfileAdapter.getLoadProfileData(loadProfiles);
    }

    @Override
    public Date getTime() {
        return this.smartMeterProtocolClockAdapter.getTime();
    }

    @Override
    public void setTime(final Date timeToSet) {
        this.smartMeterProtocolClockAdapter.setTime(timeToSet);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        return this.smartMeterProtocolLogBookAdapter.getLogBookData(logBookReaders);
    }

    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> registers) {
        return this.smartMeterProtocolRegisterAdapter.readRegisters(registers);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().getSupportedMessages();
        } else {
            return this.smartMeterProtocolMessageAdapter.getSupportedMessages();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().executePendingMessages(pendingMessages);
        } else {
            return this.smartMeterProtocolMessageAdapter.executePendingMessages(pendingMessages);
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().updateSentMessages(sentMessages);
        } else {
            return this.smartMeterProtocolMessageAdapter.updateSentMessages(sentMessages);
        }
    }

    @Override
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
        } else {
            return this.smartMeterProtocolMessageAdapter.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        this.deviceProtocolTopologyAdapter.setDeviceIdentifier(this.offlineDevice.getDeviceIdentifier());
        return this.deviceProtocolTopologyAdapter.getDeviceTopology();
    }

    @Override
    public String getVersion() {
        return this.meterProtocol.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.propertiesAdapter.copyProperties(properties);
    }

    protected SmartMeterProtocolClockAdapter getSmartMeterProtocolClockAdapter() {
        return this.smartMeterProtocolClockAdapter;
    }

    protected SmartMeterProtocolLoadProfileAdapter getSmartMeterProtocolLoadProfileAdapter() {
        return smartMeterProtocolLoadProfileAdapter;
    }

    protected SmartMeterProtocolMessageAdapter getSmartMeterProtocolMessageAdapter() {
        return smartMeterProtocolMessageAdapter;
    }

    protected DeviceProtocolTopologyAdapter getDeviceProtocolTopologyAdapter() {
        return deviceProtocolTopologyAdapter;
    }

    protected SmartMeterProtocolLogBookAdapter getSmartMeterProtocolLogBookAdapter() {
        return smartMeterProtocolLogBookAdapter;
    }

    protected SmartMeterProtocolRegisterAdapter getSmartMeterProtocolRegisterAdapter() {
        return smartMeterProtocolRegisterAdapter;
    }

    @Override
    public CachingProtocol getCachingProtocol() {
        return this.meterProtocol;
    }

    @Override
    public HHUEnabler getHhuEnabler() {
        return this.hhuEnabler;
    }

    @Override
    public void initializeLogger(Logger logger) {
        this.protocolLogger = logger;
    }

    @Override
    public void logOn() {
        try {
            this.meterProtocol.connect();
        } catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.PROTOCOL_CONNECT, e);
        }
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        try {
            this.meterProtocol.disconnect();
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.PROTOCOL_DISCONNECT, e);
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(
                new AdapterDeviceProtocolDialect(
                        thesaurus,
                        this.meterProtocol
                ));
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        this.propertiesAdapter.copyProperties(dialectProperties); // Adds all the dialectProperties to the deviceProperties
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        if (this.delegateSecurityToActualProtocol()) {
            return this.getDeviceSecuritySupport().getCustomPropertySet();
        } else {
            return this.smartMeterProtocolSecuritySupportAdapter.getCustomPropertySet();
        }
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getAuthenticationAccessLevels();
        } else {
            return this.smartMeterProtocolSecuritySupportAdapter.getAuthenticationAccessLevels();
        }
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getEncryptionAccessLevels();
        } else {
            return this.smartMeterProtocolSecuritySupportAdapter.getEncryptionAccessLevels();
        }
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
        return getDeviceSecuritySupport().getSecurityProperties();
    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.PropertySpec> getSecurityPropertySpec(String name) {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityPropertySpec(name);
        } else {
            return this.smartMeterProtocolSecuritySupportAdapter.getSecurityPropertySpec(name);
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (this.delegateSecurityToActualProtocol()) {
            getDeviceSecuritySupport().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        } else {
            this.smartMeterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
            this.meterProtocol.addProperties(this.propertiesAdapter.getProperties());
        }
        this.meterProtocol.addProperties(this.propertiesAdapter.getProperties());
    }

    private DeviceSecuritySupport getDeviceSecuritySupport() {
        return this.deviceSecuritySupport;
    }

    private boolean delegateSecurityToActualProtocol() {
        return this.deviceSecuritySupport != null;
    }

    protected void setSmartMeterProtocolSecuritySupportAdapter(SmartMeterProtocolSecuritySupportAdapter smartMeterProtocolSecuritySupportAdapter) {
        this.smartMeterProtocolSecuritySupportAdapter = smartMeterProtocolSecuritySupportAdapter;
    }

    // Publish the protected method as public
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return super.getSupportedConnectionTypes();
    }

    public SmartMeterProtocol getSmartMeterProtocol() {
        return meterProtocol;
    }

    private boolean delegateDeviceMessagesToActualProtocol() {
        return this.deviceMessageSupport != null;
    }

    /**
     * Casts the current MeterProtocol to a DeviceMessageSupport component.
     *
     * @return the deviceMessageSupport component
     */
    private DeviceMessageSupport getDeviceMessageSupport() {
        return this.deviceMessageSupport;
    }

    @Override
    protected AbstractDeviceProtocolSecuritySupportAdapter getSecuritySupportAdapter() {
        return this.smartMeterProtocolSecuritySupportAdapter;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(this.getSmartMeterProtocol().getFirmwareVersion());
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus breakerStatusCollectedData = this.collectedDataFactory.createBreakerStatusCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            Optional<BreakerStatus> breakerStatus = this.getSmartMeterProtocol().getBreakerStatus();
            if (breakerStatus.isPresent()) {
                breakerStatusCollectedData.setBreakerStatus(breakerStatus.get());
            }
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return breakerStatusCollectedData;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            this.getSmartMeterProtocol().getActiveCalendarName().ifPresent(collectedCalendar::setActiveCalendar);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        try {
            this.getSmartMeterProtocol().getPassiveCalendarName().ifPresent(collectedCalendar::setPassiveCalendar);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return collectedCalendar;
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::new).collect(Collectors.toList()));
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.propertiesAdapter.copyProperties(properties);
    }
}