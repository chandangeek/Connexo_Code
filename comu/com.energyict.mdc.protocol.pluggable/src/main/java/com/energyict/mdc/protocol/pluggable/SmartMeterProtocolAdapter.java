package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolProperty;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AdapterDeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.ComChannelInputStreamAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.ComChannelOutputStreamAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolTopologyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolClockAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolLoadProfileAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolLogBookAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolMessageAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolRegisterAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolSecuritySupportAdapter;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Adapter between a {@link SmartMeterProtocol} and a {@link DeviceProtocol}.
 *
 * @author gna
 * @since 5/04/12 - 13:13
 */
public class SmartMeterProtocolAdapter extends DeviceProtocolAdapterImpl implements DeviceProtocol {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working.
     */
    private final SmartMeterProtocol meterProtocol;

    /**
     * The use <code>IssueService</code> which can be used for this adapter.
     */
    private final IssueService issueService;
    private final MessageAdapterMappingFactory messageAdapterMappingFactory;
    private final CollectedDataFactory collectedDataFactory;

    private final MeteringService meteringService;

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
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport} functionality
     */
    private SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport} functionality
     */
    private SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport} functionality
     */
    private SmartMeterProtocolMessageAdapter smartMeterProtocolMessageAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport} functionality
     */
    private DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport} functionality
     */
    private SmartMeterProtocolLogBookAdapter smartMeterProtocolLogBookAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport} functionality
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

    public SmartMeterProtocolAdapter(SmartMeterProtocol meterProtocol, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, DataModel dataModel, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        super(propertySpecService, protocolPluggableService, securitySupportAdapterMappingFactory, dataModel, capabilityAdapterMappingFactory);
        this.messageAdapterMappingFactory = messageAdapterMappingFactory;
        this.meteringService = meteringService;
        this.protocolLogger = Logger.getAnonymousLogger(); // default for now
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
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
        this.smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory);
        this.deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter(issueService, collectedDataFactory);
        this.smartMeterProtocolLogBookAdapter = new SmartMeterProtocolLogBookAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory, meteringService);
        this.smartMeterProtocolRegisterAdapter = new SmartMeterProtocolRegisterAdapter(getSmartMeterProtocol(), issueService, collectedDataFactory);

        if (!DeviceMessageSupport.class.isAssignableFrom(getProtocolClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.smartMeterProtocolMessageAdapter = new SmartMeterProtocolMessageAdapter(getSmartMeterProtocol(), this.getDataModel(), this.messageAdapterMappingFactory, this.getProtocolPluggableService(), issueService, this.collectedDataFactory);
        }
        else {
            this.deviceMessageSupport = (DeviceMessageSupport) this.meterProtocol;
        }

        if (!DeviceSecuritySupport.class.isAssignableFrom(getProtocolClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.smartMeterProtocolSecuritySupportAdapter =
                    new SmartMeterProtocolSecuritySupportAdapter(
                            getSmartMeterProtocol(),
                            this.getPropertySpecService(),
                            this.getProtocolPluggableService(),
                            this.propertiesAdapter,
                            this.getSecuritySupportAdapterMappingFactory());
        }
        else {
            this.deviceSecuritySupport = (DeviceSecuritySupport) this.meterProtocol;
        }
    }

    protected Class getProtocolClass() {
        return this.meterProtocol.getClass();
    }

    @Override
    public void init(final OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        try {
            this.meterProtocol.init(
                    new ComChannelInputStreamAdapter(comChannel),
                    new ComChannelOutputStreamAdapter(comChannel),
                    this.getDeviceTimeZoneFromProperties(),
                    this.protocolLogger);
        }
        catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
        this.propertiesAdapter.copyProperties(comChannel.getProperties());
        if (this.smartMeterProtocolMessageAdapter != null) {
            this.smartMeterProtocolMessageAdapter.setSerialNumber(this.offlineDevice.getSerialNumber());
        }
    }

    private TimeZone getDeviceTimeZoneFromProperties() {
        TimeZone timeZone = this.propertiesAdapter.getProperties().getTypedProperty(DeviceProtocolProperty.deviceTimeZone.name());
        if (timeZone == null) {
            return TimeZone.getDefault();
        }
        else {
            return timeZone;
        }
    }

    @Override
    public void terminate() {
        try {
            this.meterProtocol.release();
        }
        catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    @Override
    public String getProtocolDescription() {
        return this.meterProtocol.getClass().getName() + "" + this.meterProtocol.getVersion();
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
        return this.getAdapterOptionalProperties();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec propertySpec : this.getPropertySpecs()) {
            if (name.equals(propertySpec.getName())) {
                return propertySpec;
            }
        }
        return null;
    }

    @Override
    public String getSerialNumber() {
        try {
            return this.meterProtocol.getMeterSerialNumber();
        }
        catch (IOException e) {
            throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
        }
    }

    @Override
    public void setTime(final Date timeToSet) {
        this.smartMeterProtocolClockAdapter.setTime(timeToSet);
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
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        return this.smartMeterProtocolLogBookAdapter.getLogBookData(logBookReaders);
    }

    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> registers) {
        return this.smartMeterProtocolRegisterAdapter.readRegisters(registers);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().getSupportedMessages();
        }
        else {
            return this.smartMeterProtocolMessageAdapter.getSupportedMessages();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().executePendingMessages(pendingMessages);
        }
        else {
            return this.smartMeterProtocolMessageAdapter.executePendingMessages(pendingMessages);
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().updateSentMessages(sentMessages);
        }
        else {
            return this.smartMeterProtocolMessageAdapter.updateSentMessages(sentMessages);
        }
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().format(propertySpec, messageAttribute);
        }
        else {
            return this.smartMeterProtocolMessageAdapter.format(propertySpec, messageAttribute);
        }
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
        }
        catch (IOException e) {
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
        }
        catch (IOException e) {
            throw new CommunicationException(MessageSeeds.PROTOCOL_DISCONNECT, e);
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>(1);
        dialects.add(new AdapterDeviceProtocolDialect(this.getPropertySpecService(), this.getProtocolPluggableService(), this.meterProtocol, getSecurityPropertySpecs()));
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.propertiesAdapter.copyProperties(dialectProperties); // Adds all the dialectProperties to the deviceProperties
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityPropertySpecs();
        }
        else {
            return this.smartMeterProtocolSecuritySupportAdapter.getSecurityPropertySpecs();
        }
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getAuthenticationAccessLevels();
        }
        else {
            return this.smartMeterProtocolSecuritySupportAdapter.getAuthenticationAccessLevels();
        }
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getEncryptionAccessLevels();
        }
        else {
            return this.smartMeterProtocolSecuritySupportAdapter.getEncryptionAccessLevels();
        }
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityPropertySpec(name);
        }
        else {
            return this.smartMeterProtocolSecuritySupportAdapter.getSecurityPropertySpec(name);
        }
    }

    @Override
    public String getSecurityRelationTypeName() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityRelationTypeName();
        }
        else {
            return this.smartMeterProtocolSecuritySupportAdapter.getSecurityRelationTypeName();
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (this.delegateSecurityToActualProtocol()) {
            getDeviceSecuritySupport().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        }
        else {
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

    protected SmartMeterProtocol getSmartMeterProtocol() {
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
}