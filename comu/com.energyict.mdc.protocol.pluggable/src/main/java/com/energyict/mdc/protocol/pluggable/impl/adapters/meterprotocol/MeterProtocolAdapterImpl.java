/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceSecuritySupport;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannelInputStreamAdapter;
import com.energyict.mdc.io.ComChannelOutputStreamAdapter;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AdapterDeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolTopologyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLOfflineDeviceAdapter;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.SerialNumberSupport;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedData;
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
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.upl.tasks.support.DeviceClockSupport;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.exceptions.CommunicationException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link MeterProtocol} and a {@link DeviceProtocol}.
 *
 * @author gna
 * @since 29/03/12 - 9:08
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class MeterProtocolAdapterImpl extends DeviceProtocolAdapterImpl implements DeviceProtocol, MeterProtocolAdapter {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working.
     */
    private MeterProtocol meterProtocol;

    /**
     * The use <code>IssueService</code> which can be used for this adapter.
     */
    private IssueService issueService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private MessageAdapterMappingFactory messageAdapterMappingFactory;
    private CollectedDataFactory collectedDataFactory;
    private Thesaurus thesaurus;
    private IdentificationService identificationService;
    private ComChannelInputStreamAdapter comChannelInputStreamAdapter;
    private ComChannelOutputStreamAdapter comChannelOutputStreamAdapter;

    /**
     * The used <code>RegisterProtocol</code> for which the adapter is working.
     */
    private RegisterProtocol registerProtocol;

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
     * The Adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport} functionality
     */
    private MeterProtocolRegisterAdapter meterProtocolRegisterAdapter;

    /**
     * The adapter used for the {@link DeviceClockSupport} functionality
     */
    private MeterProtocolClockAdapter meterProtocolClockAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport} functionality
     */
    private MeterProtocolLoadProfileAdapter meterProtocolLoadProfileAdapter;

    /**
     * The adapter used for the {@link DeviceMessageSupport} functionality.
     */
    private MeterProtocolMessageAdapter meterProtocolMessageAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.upl.tasks.support.DeviceTopologySupport} functionality
     */
    private DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter;

    /**
     * The adapter used for the {@link DeviceSecuritySupport} functionality.
     */
    private MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter;

    /**
     * The adapter used for the property handling.
     */
    private PropertiesAdapter propertiesAdapter;

    /**
     * The logger used by the protocol.
     */
    private Logger protocolLogger;

    /**
     * The used HHUEnabler.
     */
    private HHUEnabler hhuEnabler;

    public MeterProtocolAdapterImpl(){
    }

    public MeterProtocolAdapterImpl(MeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, DataModel dataModel, IssueService issueService, CollectedDataFactory collectedDataFactory, IdentificationService identificationService, Thesaurus thesaurus, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(propertySpecService, protocolPluggableService, thesaurus, securitySupportAdapterMappingFactory, dataModel, capabilityAdapterMappingFactory);
        this.messageAdapterMappingFactory = messageAdapterMappingFactory;
        this.identificationService = identificationService;
        this.thesaurus = thesaurus;
        this.protocolLogger = Logger.getAnonymousLogger(); // default for now
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        if (getUplMeterProtocol() instanceof RegisterProtocol) {
            this.registerProtocol = (RegisterProtocol) getUplMeterProtocol();
        } else {
            this.registerProtocol = null;
        }
        initializeAdapters();
        initInheritors();
    }

    private ComChannelInputStreamAdapter getComChannelInputStreamAdapter(ComChannel comChannel) {
        if (comChannelInputStreamAdapter==null) {
            comChannelInputStreamAdapter= new ComChannelInputStreamAdapter(comChannel);
        }
        return comChannelInputStreamAdapter;
    }

    private ComChannelOutputStreamAdapter getComChannelOutputStreamAdapter(ComChannel comChannel) {
        if (comChannelOutputStreamAdapter==null) {
            comChannelOutputStreamAdapter= new ComChannelOutputStreamAdapter(comChannel);
        }
        return comChannelOutputStreamAdapter;
    }

    /**
     * Initializes the inheritance classes.
     */
    private void initInheritors() {
        if (getUplMeterProtocol() instanceof HHUEnabler) {
            this.hhuEnabler = (HHUEnabler) getUplMeterProtocol();
        }
    }

    /**
     * Initializes the adapters so they can be used in the {@link DeviceProtocol} calls.
     */
    protected void initializeAdapters() {
        this.propertiesAdapter = new PropertiesAdapter();
        this.meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(registerProtocol, issueService, collectedDataFactory);
        this.meterProtocolLoadProfileAdapter = new MeterProtocolLoadProfileAdapter(meterProtocol, issueService, collectedDataFactory, identificationService);
        this.meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        this.deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter(issueService, collectedDataFactory);

        if (!DeviceMessageSupport.class.isAssignableFrom(getProtocolClass())) {
            this.meterProtocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.messageAdapterMappingFactory, this.getProtocolPluggableService(), this.issueService, this.collectedDataFactory, this.deviceMessageSpecificationService);
        } else {
            this.deviceMessageSupport = (DeviceMessageSupport) this.meterProtocol;
        }

        if (!DeviceSecuritySupport.class.isAssignableFrom(getProtocolClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.meterProtocolSecuritySupportAdapter =
                    new MeterProtocolSecuritySupportAdapter(
                            this.meterProtocol,
                            this.getPropertySpecService(),
                            this.getProtocolPluggableService(),
                            this.propertiesAdapter,
                            this.getSecuritySupportAdapterMappingFactory());
        } else {
            this.deviceSecuritySupport = (DeviceSecuritySupport) this.meterProtocol;
        }
    }

    @Override
    public void init(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = new UPLOfflineDeviceAdapter(offlineDevice);
        meterProtocolLoadProfileAdapter.setOfflineDevice(this.offlineDevice);
        doInit(comChannel);
    }

    private void doInit(ComChannel comChannel) {
        try {
            this.meterProtocol.init(
                    getComChannelInputStreamAdapter(comChannel),
                    getComChannelOutputStreamAdapter(comChannel),
                    this.getDeviceTimeZoneFromProperties(),
                    this.protocolLogger);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        this.propertiesAdapter.copyProperties(comChannel.getProperties());
    }

    @Override
    public LegacyMessageConverter getLegacyMessageConverter() {
        return this.meterProtocolMessageAdapter.getLegacyMessageConverter();
    }

    private TimeZone getDeviceTimeZoneFromProperties() {
        TimeZone timeZone = this.propertiesAdapter.getProperties().getTypedProperty(LegacyProtocolProperties.DEVICE_TIMEZONE_PROPERTY_NAME);
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
            if (comChannelInputStreamAdapter!=null) {
                comChannelInputStreamAdapter.close();
            }
            if (comChannelOutputStreamAdapter!=null) {
                comChannelOutputStreamAdapter.close();
            }
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    @Override
    public String getProtocolDescription() {
        return meterProtocol.getProtocolDescription();
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.METER;    //TODO: is this correct?
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
        if (meterProtocol instanceof SerialNumberSupport) {
            return ((SerialNumberSupport) meterProtocol).getSerialNumber();
        } else if (meterProtocol instanceof UPLMeterProtocolAdapter &&
                ((UPLMeterProtocolAdapter) meterProtocol).getActual() != null &&
                ((UPLMeterProtocolAdapter) meterProtocol).getActual() instanceof SerialNumberSupport) {
            // old V1 protocols
            return ((SerialNumberSupport) ((UPLMeterProtocolAdapter) meterProtocol).getActual()).getSerialNumber();
        } else {
           throw new CommunicationException(MessageSeeds.SERIAL_NUMBER_NOT_SUPPORTED);
        }
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        return this.meterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(final List<LoadProfileReader> loadProfiles) {
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(MessageSeeds.UNSUPPORTED_METHOD, this.getClass(), "getLoadProfileData");
    }

    @Override
    public List<CollectedData> getLoadProfileLogBooksData(final List<LoadProfileReader> loadProfiles, final List<LogBookReader> logBookReaders) {
        return this.meterProtocolLoadProfileAdapter.getLoadProfileLogBookData(loadProfiles, logBookReaders);
    }

    public LogBookReader getValidLogBook(List<LogBookReader> logBookReaders) {
        return this.meterProtocolLoadProfileAdapter.getValidLogBook(logBookReaders);
    }

    @Override
    public Date getTime() {
        return this.meterProtocolClockAdapter.getTime();
    }

    @Override
    public void setTime(final Date timeToSet) {
        this.meterProtocolClockAdapter.setTime(timeToSet);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(MessageSeeds.UNSUPPORTED_METHOD, this.getClass(), "getLogBookData");
    }

    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> registers) {
        return this.meterProtocolRegisterAdapter.readRegisters(registers);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().getSupportedMessages();
        } else {
            return this.meterProtocolMessageAdapter.getSupportedMessages();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().executePendingMessages(pendingMessages);
        } else {
            return this.meterProtocolMessageAdapter.executePendingMessages(pendingMessages);
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().updateSentMessages(sentMessages);
        } else {
            return this.meterProtocolMessageAdapter.updateSentMessages(sentMessages);
        }
    }

    @Override
    public String format(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
        } else {
            return this.meterProtocolMessageAdapter.format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
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
        return this.meterProtocol.getProtocolVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.propertiesAdapter.copyProperties(properties);
    }

    protected MeterProtocolRegisterAdapter getMeterProtocolRegisterAdapter() {
        return meterProtocolRegisterAdapter;
    }

    protected MeterProtocolClockAdapter getMeterProtocolClockAdapter() {
        return meterProtocolClockAdapter;
    }

    protected MeterProtocolLoadProfileAdapter getMeterProtocolLoadProfileAdapter() {
        return meterProtocolLoadProfileAdapter;
    }

    protected DeviceProtocolTopologyAdapter getDeviceProtocolTopologyAdapter() {
        return deviceProtocolTopologyAdapter;
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
            throw new CommunicationException(e, MessageSeeds.PROTOCOL_CONNECT);
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
            throw new CommunicationException(e, MessageSeeds.PROTOCOL_DISCONNECT);
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>(1);
        dialects.add(new AdapterDeviceProtocolDialect(thesaurus, this.meterProtocol));
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(com.energyict.mdc.upl.properties.TypedProperties dialectProperties) {
        this.propertiesAdapter.copyProperties(dialectProperties); // Adds all the dialectProperties to the deviceProperties
    }

    /**
     * This <i>forwards</i> the {@link PropertiesAdapter#getProperties()} to the {@link MeterProtocol} via the
     * {@link MeterProtocol#setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties)} method.
     * <p>
     * <b>This should happen only once!</b>
     */
    private void setPropertiesToMeterProtocol() {
        try {
            this.meterProtocol.setUPLProperties(this.propertiesAdapter.getProperties());
        } catch (InvalidPropertyException | MissingPropertyException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return new ArrayList<>(getPropertySpecs().stream().map(ConnexoToUPLPropertSpecAdapter::adaptTo).collect(Collectors.toList()));
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        this.propertiesAdapter.copyProperties(properties);
    }

    @Override
    public Optional<com.energyict.mdc.upl.properties.PropertySpec> getClientSecurityPropertySpec() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getClientSecurityPropertySpec();
        } else {
            return this.meterProtocolSecuritySupportAdapter.getClientSecurityPropertySpec();
        }
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getAuthenticationAccessLevels();
        } else {
            return this.meterProtocolSecuritySupportAdapter.getAuthenticationAccessLevels();
        }
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getEncryptionAccessLevels();
        } else {
            return this.meterProtocolSecuritySupportAdapter.getEncryptionAccessLevels();
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (this.delegateSecurityToActualProtocol()) {
            getDeviceSecuritySupport().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        } else {
            this.meterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        }
        setPropertiesToMeterProtocol();
    }

    private boolean delegateSecurityToActualProtocol() {
        return this.deviceSecuritySupport != null;
    }

    /**
     * Casts the current MeterProtocol to a DeviceSecuritySupport component.
     *
     * @return the deviceSecuritySupport component
     */
    private DeviceSecuritySupport getDeviceSecuritySupport() {
        return this.deviceSecuritySupport;
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

    protected void setMeterProtocolSecuritySupportAdapter(MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter) {
        this.meterProtocolSecuritySupportAdapter = meterProtocolSecuritySupportAdapter;
    }

    // Publish the protected method as public
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return super.getSupportedConnectionTypes();
    }

    @Override
    protected Class getProtocolClass() {
        return getUplMeterProtocol().getClass();
    }

    @Override
    public MeterProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public com.energyict.mdc.upl.MeterProtocol getUplMeterProtocol() {
        return (this.meterProtocol instanceof UPLProtocolAdapter)
                    ? (com.energyict.mdc.upl.MeterProtocol) ((UPLProtocolAdapter) this.meterProtocol).getActual()
                    : this.meterProtocol;
    }

    @Override
    protected AbstractDeviceProtocolSecuritySupportAdapter getSecuritySupportAdapter() {
        return this.meterProtocolSecuritySupportAdapter;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = this.collectedDataFactory.createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(this.getMeterProtocol().getFirmwareVersion());
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return firmwareVersionsCollectedData;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus breakerStatusCollectedData = this.collectedDataFactory.createBreakerStatusCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            Optional<BreakerStatus> breakerStatus = this.getMeterProtocol().getBreakerStatus();
            breakerStatus.ifPresent(breakerStatusCollectedData::setBreakerStatus);
            if (!breakerStatus.isPresent()) {
                final Optional<BreakerStatus> breakerStatusUpl = this.getUplMeterProtocol().getBreakerStatus();
                breakerStatusUpl.ifPresent(breakerStatusCollectedData::setBreakerStatus);
            }
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return breakerStatusCollectedData;
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        CollectedCreditAmount creditAmountCollectedData = this.collectedDataFactory.createCreditAmountCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            Optional<BigDecimal> creditAmount = this.getMeterProtocol().getCreditAmount();
            creditAmount.ifPresent(creditAmountCollectedData::setCreditAmount);
            String creditType = this.getMeterProtocol().getCreditType();
            creditAmountCollectedData.setCreditType(creditType);
            if (!creditAmount.isPresent()) {
                final Optional<BigDecimal> creditAmountUpl = this.getUplMeterProtocol().getCreditAmount();
                creditAmountUpl.ifPresent(creditAmountCollectedData::setCreditAmount);
                String creditTypeUpl = this.getMeterProtocol().getCreditType();
                creditAmountCollectedData.setCreditType(creditTypeUpl);
            }
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return creditAmountCollectedData;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        CollectedCalendar collectedCalendar = this.collectedDataFactory.createCalendarCollectedData(this.offlineDevice.getDeviceIdentifier());
        try {
            this.getMeterProtocol().getActiveCalendarName().ifPresent(collectedCalendar::setActiveCalendar);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        try {
            this.getMeterProtocol().getPassiveCalendarName().ifPresent(collectedCalendar::setPassiveCalendar);
        } catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        return collectedCalendar;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}