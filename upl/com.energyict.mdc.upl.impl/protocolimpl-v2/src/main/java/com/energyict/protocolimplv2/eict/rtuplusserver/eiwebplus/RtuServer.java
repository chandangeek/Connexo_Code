package com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.inbound.EIWebPlusConnectionType;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.LegacyMessageConverter;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.EiWebPlusDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.messages.convertor.EIWebPlusMessageConverter;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Place holder protocol for the RTU+Server concentrator.
 * This protocol provides all messages from the old V1 protocol (com.energyict.rtuprotocol.RtuServer)
 * These messages are used to configure the RTU+Server and execute some of its functions.
 * <p/>
 * Note: this protocol does not provide an implementation to execute the messages, nor does it provide any kind of
 * functionality to read out meter data, this is all done by the EIWebPlus servlet. This servlet runs in a separate tomcat, not in the comserver.
 * <p/>
 * In order to create a valid connection task on this concentrator device, it should have a communication port pool (a place holder that is empty) and a connection type {@link EIWebPlusConnectionType}.
 * On this connection task, you can then define a connection strategy (read now vs read every X), this configures when the RTU+Server will post its the collected data.
 * Note2: in order to prevent this device from being executed by the comserver, its communication port pool should be a placeholder that is empty (has no ports).
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/03/14
 * Time: 13:40
 * Author: khe
 */
public class RtuServer implements DeviceProtocol {

    private OfflineDevice offlineDevice;
    private NoOrPasswordSecuritySupport securitySupport = new NoOrPasswordSecuritySupport();
    private LegacyMessageConverter messageConverter;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        // nothing much to do
        this.offlineDevice = offlineDevice;
    }

    @Override
    public void terminate() {
        // nothing much to do
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.<ConnectionType>asList(new EIWebPlusConnectionType());
    }

    @Override
    public void logOn() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOn() {
        // nothing much to do
    }

    @Override
    public void logOff() {
        // nothing much to do
    }

    @Override
    public void daisyChainedLogOff() {
        // nothing much to do
    }

    @Override
    public String getSerialNumber() {
        return this.offlineDevice != null ? this.offlineDevice.getSerialNumber() : "";
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // nothing much to do
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return Collections.emptyList();
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return Collections.emptyList();
    }

    @Override
    public Date getTime() {
        return new Date();
    }

    @Override
    public void setTime(Date timeToSet) {
        // nothing much to do
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessageConverter().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();     //Messages are executed in ProtocolHandler, not here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    private LegacyMessageConverter getMessageConverter() {
        if (messageConverter == null) {
            messageConverter = new EIWebPlusMessageConverter();
        }
        return messageConverter;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new EiWebPlusDialect());
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        // nothing much to do
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        // nothing much to do
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return this.securitySupport.getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return this.securitySupport.getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return this.securitySupport.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return this.securitySupport.getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return this.securitySupport.getSecurityPropertySpec(name);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return Collections.emptyList();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU+Server EIWebPlus V2";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // nothing much to do
    }
}