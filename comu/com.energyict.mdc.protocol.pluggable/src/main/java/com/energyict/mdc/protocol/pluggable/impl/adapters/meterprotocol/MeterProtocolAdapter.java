package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceSecuritySupport;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AbstractDeviceProtocolSecuritySupportAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.AdapterDeviceProtocolDialect;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.ComChannelInputStreamAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.ComChannelOutputStreamAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DeviceProtocolTopologyAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.PropertiesAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Adapter between a {@link MeterProtocol} and a {@link DeviceProtocol}
 *
 * @author gna
 * @since 29/03/12 - 9:08
 */
public class MeterProtocolAdapter extends DeviceProtocolAdapterImpl implements DeviceProtocol {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working
     */
    private final MeterProtocol meterProtocol;

    /**
     * The used <code>RegisterProtocol</code> for which the adapter is working
     */
    private final RegisterProtocol registerProtocol;

    /**
     * The DeviceSecuritySupport component that <i>can</i> be used during communication
     */
    private DeviceSecuritySupport deviceSecuritySupport;

    /**
     * The DeviceMessageSupport component that <i>can</i> be used during communication
     */
    private DeviceMessageSupport deviceMessageSupport;

    /**
     * The offline device for which this adapter is working
     */
    private OfflineDevice offlineDevice;

    /**
     * The Adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport} functionality
     */
    private MeterProtocolRegisterAdapter meterProtocolRegisterAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport} functionality
     */
    private MeterProtocolClockAdapter meterProtocolClockAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport} functionality
     */
    private MeterProtocolLoadProfileAdapter meterProtocolLoadProfileAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport} functionality
     */
    private MeterProtocolMessageAdapter meterProtocolMessageAdapter;

    /**
     * The adapter used for the {@link com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport} functionality
     */
    private DeviceProtocolTopologyAdapter deviceProtocolTopologyAdapter;

    /**
     * The adapter used for the {@link DeviceSecuritySupport} functionality
     */
    private MeterProtocolSecuritySupportAdapter meterProtocolSecuritySupportAdapter;

    /**
     * The adapter used for the property handling
     */
    private PropertiesAdapter propertiesAdapter;

    /**
     * The logger used by the protocol
     */
    private Logger protocolLogger;

    /**
     * The used HHUEnabler
     */
    private HHUEnabler hhuEnabler;

    public MeterProtocolAdapter(final MeterProtocol meterProtocol, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, DataModel dataModel) {
        super(protocolPluggableService, securitySupportAdapterMappingFactory, dataModel);
        this.meterProtocol = meterProtocol;
        if (meterProtocol instanceof RegisterProtocol) {
            this.registerProtocol = (RegisterProtocol) meterProtocol;
        }
        else {
            this.registerProtocol = null;
        }
        initializeAdapters();
        initInheritors();
    }

    /**
     * Initializes the inheritance classes
     */
    private void initInheritors() {
        if (this.meterProtocol instanceof HHUEnabler) {
            this.hhuEnabler = (HHUEnabler) this.meterProtocol;
        }
    }

    /**
     * Initializes the adapters so they can be used in the {@link DeviceProtocol} calls
     */
    protected void initializeAdapters() {
        this.propertiesAdapter = new PropertiesAdapter();
        this.meterProtocolRegisterAdapter = new MeterProtocolRegisterAdapter(registerProtocol);
        this.meterProtocolLoadProfileAdapter = new MeterProtocolLoadProfileAdapter(meterProtocol);
        this.meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
        this.deviceProtocolTopologyAdapter = new DeviceProtocolTopologyAdapter();

        if (!DeviceMessageSupport.class.isAssignableFrom(this.meterProtocol.getClass())) {
            this.meterProtocolMessageAdapter = new MeterProtocolMessageAdapter(meterProtocol, this.getDataModel());
        }
        else {
            this.deviceMessageSupport = (DeviceMessageSupport) this.meterProtocol;
        }

        if (!DeviceSecuritySupport.class.isAssignableFrom(this.meterProtocol.getClass())) {
            // we only instantiate the adapter if the protocol needs it
            this.meterProtocolSecuritySupportAdapter =
                    new MeterProtocolSecuritySupportAdapter(
                            this.meterProtocol,
                            this.getPropertySpecService(),
                            this.propertiesAdapter,
                            this.getSecuritySupportAdapterMappingFactory());
        }
        else {
            this.deviceSecuritySupport = (DeviceSecuritySupport) this.meterProtocol;
        }
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
            throw new LegacyProtocolException(e);
        }
        this.propertiesAdapter.copyProperties(comChannel.getProperties());
        if (this.meterProtocolMessageAdapter != null) {
            this.meterProtocolMessageAdapter.setSerialNumber(this.offlineDevice.getSerialNumber());
        }
    }

    private TimeZone getDeviceTimeZoneFromProperties() {
        TimeZone timeZone = this.propertiesAdapter.getProperties().getTypedProperty(DEVICE_TIMEZONE_PROPERTY_NAME);
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
            throw new LegacyProtocolException(e);
        }
    }

    @Override
    public String getProtocolDescription() {
        return "";
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
        return this.offlineDevice.getSerialNumber();
    }

    @Override
    public void setTime(final Date timeToSet) {
        this.meterProtocolClockAdapter.setTime(timeToSet);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        return this.meterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(final List<LoadProfileReader> loadProfiles) {
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    public List<CollectedData> getLoadProfileLogBooksData(final List<LoadProfileReader> loadProfiles, final List<LogBookReader> logBookReaders) {
        return this.meterProtocolLoadProfileAdapter.getLoadProfileLogBookData(loadProfiles, logBookReaders);
    }

    @Override
    public Date getTime() {
        return this.meterProtocolClockAdapter.getTime();
    }

    @Override
    public List<CollectedLogBook> getLogBookData(final List<LogBookReader> logBookReaders) {
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(this.getClass(), "getLogBookData");
    }

    @Override
    public List<CollectedRegister> readRegisters(final List<OfflineRegister> registers) {
        return this.meterProtocolRegisterAdapter.readRegisters(registers);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().getSupportedMessages();
        }
        else {
            return this.meterProtocolMessageAdapter.getSupportedMessages();
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().executePendingMessages(pendingMessages);
        }
        else {
            return this.meterProtocolMessageAdapter.executePendingMessages(pendingMessages);
        }
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().updateSentMessages(sentMessages);
        }
        else {
            return this.meterProtocolMessageAdapter.updateSentMessages(sentMessages);
        }
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (delegateDeviceMessagesToActualProtocol()) {
            return getDeviceMessageSupport().format(propertySpec, messageAttribute);
        }
        else {
            return this.meterProtocolMessageAdapter.format(propertySpec, messageAttribute);
        }
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        this.deviceProtocolTopologyAdapter.setDeviceIdentifier(new SerialNumberDeviceIdentifier(this.offlineDevice.getSerialNumber()));
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

    protected MeterProtocolMessageAdapter getMeterProtocolMessageAdapter() {
        return meterProtocolMessageAdapter;
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
        }
        catch (IOException e) {
            throw CommunicationException.protocolConnectFailed(e);
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
            throw CommunicationException.protocolDisconnectFailed(e);
        }
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialects = new ArrayList<>(1);
        dialects.add(new AdapterDeviceProtocolDialect(this.getPropertySpecService(), this.getProtocolPluggableService(), this.meterProtocol, getSecurityProperties()));
        return dialects;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        this.propertiesAdapter.copyProperties(dialectProperties); // Adds all the dialectProperties to the deviceProperties
    }

    /**
     * This <i>forwards</i> the {@link PropertiesAdapter#getProperties()} to the {@link MeterProtocol} via the
     * {@link MeterProtocol#setProperties(java.util.Properties)} method.
     * <p/>
     * <b>This should happen only once!</b>
     */
    private void setPropertiesToMeterProtocol() {
        try {
            this.meterProtocol.setProperties(this.propertiesAdapter.getProperties().toStringProperties());
        }
        catch (InvalidPropertyException | MissingPropertyException e) {
            throw new LegacyProtocolException(e);
        }
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityProperties();
        }
        else {
            return this.meterProtocolSecuritySupportAdapter.getSecurityProperties();
        }
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getAuthenticationAccessLevels();
        }
        else {
            return this.meterProtocolSecuritySupportAdapter.getAuthenticationAccessLevels();
        }
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getEncryptionAccessLevels();
        }
        else {
            return this.meterProtocolSecuritySupportAdapter.getEncryptionAccessLevels();
        }
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityPropertySpec(name);
        }
        else {
            return this.meterProtocolSecuritySupportAdapter.getSecurityPropertySpec(name);
        }
    }

    @Override
    public String getSecurityRelationTypeName() {
        if (this.delegateSecurityToActualProtocol()) {
            return getDeviceSecuritySupport().getSecurityRelationTypeName();
        }
        else {
            return this.meterProtocolSecuritySupportAdapter.getSecurityRelationTypeName();
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        if (this.delegateSecurityToActualProtocol()) {
            getDeviceSecuritySupport().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        }
        else {
            this.meterProtocolSecuritySupportAdapter.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        }
        setPropertiesToMeterProtocol();
    }

    private boolean delegateSecurityToActualProtocol() {
        return this.deviceSecuritySupport != null;
    }

    /**
     * Casts the current MeterProtocol to a DeviceSecuritySupport component
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
     * Casts the current MeterProtocol to a DeviceMessageSupport component
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
        return meterProtocol.getClass();
    }

    protected MeterProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    protected AbstractDeviceProtocolSecuritySupportAdapter getSecuritySupportAdapter() {
        return this.meterProtocolSecuritySupportAdapter;
    }

}