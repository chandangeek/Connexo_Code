package com.energyict.protocolimplv2.nta.abstractnta;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.*;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;
import com.energyict.protocols.exception.UnsupportedMethodException;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The Abstract NTA Mbus device implements the {@link DeviceProtocol} interface so we can
 * define this as a pluggable class in EIS 9.1.
 * Most of the methods throw an unsupportedMethod CodingException, if your subclass wants
 * to use one of these, then simply override them.
 * <p/>
 *
 * @author sva
 * @since 2/11/12 (9:11)
 */
public abstract class AbstractNtaMbusDevice implements DeviceProtocol {

    private final PropertySpecService propertySpecService;
    private final AbstractDlmsProtocol meterProtocol;
    private final String serialNumber;
    private final int physicalAddress;
    private final TopologyService topologyService;

    public AbstractNtaMbusDevice(Clock clock, PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, LoadProfileFactory loadProfileFactory) {
        this.propertySpecService = propertySpecService;
        this.topologyService = topologyService;
        //TODO, what? wait! Is this even correct?
        this.meterProtocol = new WebRTUKP(clock, this.propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory);
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
    }

    /**
     * Get the used MessageProtocol
     *
     * @return the DeviceMessageSupport message protocol
     */

    public abstract DeviceMessageSupport getDeviceMessageSupport();

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected TopologyService getTopologyService() {
        return topologyService;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>(0);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getDeviceMessageSupport().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDeviceMessageSupport().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDeviceMessageSupport().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDeviceMessageSupport().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList((DeviceProtocolDialect) new NoParamsDeviceProtocolDialect(propertySpecService));
    }

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    /**
     * Getter for the master {@link AbstractDlmsProtocol}
     *
     * @return the protocol of the master
     */
    public AbstractDlmsProtocol getMeterProtocol() {
        return meterProtocol;
    }

    @Override
    public List<PropertySpec> getSecurityPropertySpecs() {
        return getMeterProtocol().getSecurityPropertySpecs();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getMeterProtocol().getSecurityRelationTypeName();
    }

    /**
     * Return the access levels of the master AND a dummy level that indicates that this device can also
     * simply inherit the security properties of the master device, instead of specifying the security properties again
     */
    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.addAll(getMeterProtocol().getAuthenticationAccessLevels());
        authenticationAccessLevels.add(new InheritedAuthenticationDeviceAccessLevel());
        return authenticationAccessLevels;
    }


    /**
     * Return the access levels of the master AND a dummy level that indicates that this device can also
     * simply inherit the security properties of the master device, instead of specifying the security properties again
     */
    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = new ArrayList<>();
        encryptionAccessLevels.addAll(getMeterProtocol().getEncryptionAccessLevels());
        encryptionAccessLevels.add(new InheritedEncryptionDeviceAccessLevel());
        return encryptionAccessLevels;
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getMeterProtocol().getSecurityPropertySpec(name);
    }

    //############## Unsupported methods ##############//

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        throw new UnsupportedMethodException(this.getClass(), "init");
    }

    @Override
    public void terminate() {
        throw new UnsupportedMethodException(this.getClass(), "terminate");
    }

    @Override
    public void logOn() {
        throw new UnsupportedMethodException(this.getClass(), "longOn");
    }

    @Override
    public void daisyChainedLogOn() {
        throw new UnsupportedMethodException(this.getClass(), "daisyChainedLogOn");
    }

    @Override
    public void logOff() {
        throw new UnsupportedMethodException(this.getClass(), "logOff");
    }

    @Override
    public void daisyChainedLogOff() {
        throw new UnsupportedMethodException(this.getClass(), "daisyChainedLogOff");
    }

    @Override
    public void setTime(Date timeToSet) {
        throw new UnsupportedMethodException(this.getClass(), "setTime");
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // Using NoParamsDeviceProtocolDialect so nothign to add here
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        throw new UnsupportedMethodException(this.getClass(), "setSecurityPropertySet");
    }

    @Override
    public String getVersion() {
        return null;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        throw new UnsupportedMethodException(this.getClass(), "fetchLoadProfileConfiguration");
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw new UnsupportedMethodException(this.getClass(), "getLoadProfileData");
    }

    @Override
    public Date getTime() {
        throw new UnsupportedMethodException(this.getClass(), "getTime");
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        throw new UnsupportedMethodException(this.getClass(), "setDeviceCache");
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        throw new UnsupportedMethodException(this.getClass(), "getDeviceCache");
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        throw new UnsupportedMethodException(this.getClass(), "getLogBookData");
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        throw new UnsupportedMethodException(this.getClass(), "readRegisters");
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        throw new UnsupportedMethodException(this.getClass(), "getDeviceTopology");
    }



    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec(String s) {
        return null;
    }


    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        throw new UnsupportedMethodException(this.getClass(), "getFirmwareVersions");
    }
}