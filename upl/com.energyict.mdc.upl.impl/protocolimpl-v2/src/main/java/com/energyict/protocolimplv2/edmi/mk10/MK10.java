package com.energyict.protocolimplv2.edmi.mk10;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedTopology;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimpl.edmi.common.connection.CommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.ExtendedCommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.MiniECommandLineConnection;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10Register;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;
import com.energyict.protocolimplv2.edmi.dialects.OpticalHeadDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.UdpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.mk10.events.MK10LogBookFactory;
import com.energyict.protocolimplv2.edmi.mk10.profiles.MK10LoadProfileBuilder;
import com.energyict.protocolimplv2.edmi.mk10.properties.MK10ConfigurationSupport;
import com.energyict.protocolimplv2.edmi.mk10.properties.MK10Properties;
import com.energyict.protocolimplv2.edmi.mk10.registers.MK10RegisterFactory;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.SimplePasswordSecuritySupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author sva
 * @since 22/02/2017 - 16:02
 */
public class MK10 implements DeviceProtocol, CommandLineProtocol {

    private ComChannel comChannel;
    private OfflineDevice offlineDevice;
    private MK10ConfigurationSupport configurationSupport;
    private SimplePasswordSecuritySupport securitySupport;
    private CommandLineConnection commandLineConnection;
    private CommandFactory commandFactory;
    private MK10Properties properties;
    private MK10LogBookFactory logBookFactory;
    private MK10LoadProfileBuilder loadProfileBuilder;
    private MK10RegisterFactory registerFactory;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return getConfigurationSupport().getRequiredProperties();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return getConfigurationSupport().getOptionalProperties();
    }

    protected MK10ConfigurationSupport getConfigurationSupport() {
        if (configurationSupport == null) {
            configurationSupport = new MK10ConfigurationSupport();
        }
        return configurationSupport;
    }

    private MK10Properties getProperties() {
        if (properties == null) {
            properties = new MK10Properties();
        }
        return properties;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundUdpConnectionType());
        result.add(new OutboundTcpIpConnectionType());
        result.add(new InboundIpConnectionType());
        return result;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> result = new ArrayList<>();
        result.add(new TcpDeviceProtocolDialect());
        result.add(new UdpDeviceProtocolDialect());
        result.add(new OpticalHeadDeviceProtocolDialect());
        return result;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getProperties().addDeviceProtocolDialectProperties(dialectProperties);
    }

    @Override
    public void addProperties(TypedProperties properties) {
        getProperties().addProperties(properties);
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        getProperties().setSecurityPropertySet(deviceProtocolSecurityPropertySet);
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return getSecuritySupport().getSecurityProperties();
    }

    @Override
    public String getSecurityRelationTypeName() {
        return getSecuritySupport().getSecurityRelationTypeName();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    @Override
    public PropertySpec getSecurityPropertySpec(String name) {
        return getSecuritySupport().getSecurityPropertySpec(name);
    }

    public SimplePasswordSecuritySupport getSecuritySupport() {
        if (this.securitySupport == null) {
            this.securitySupport = new SimplePasswordSecuritySupport();
        }
        return this.securitySupport;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public CommandFactory getCommandFactory() {
        if (commandFactory == null) {
            commandFactory = new CommandFactory(this);
        }
        return commandFactory;
    }

    @Override
    public CommandLineConnection getCommandLineConnection() {
        if (commandLineConnection == null) {
            if (getProperties().getConnectionMode().equals(MK10ConfigurationSupport.ConnectionMode.MINI_E_COMMAND_LINE)) {
                commandLineConnection = new MiniECommandLineConnection(
                        new ComChannelInputStreamAdapter(comChannel),
                        new ComChannelOutputStreamAdapter(comChannel),
                        getProperties().getTimeout(),
                        getProperties().getMaxRetries(),
                        getProperties().getforcedDelay(),
                        0,
                        null,
                        getOfflineDevice().getSerialNumber()
                );
            } else {
                commandLineConnection = new ExtendedCommandLineConnection(
                        getComChannel(),
                        new ComChannelInputStreamAdapter(comChannel),
                        new ComChannelOutputStreamAdapter(comChannel),
                        getProperties().getTimeout(),
                        getProperties().getMaxRetries(),
                        getProperties().getforcedDelay(),
                        0,
                        null,
                        getOfflineDevice().getSerialNumber()
                );
            }
        }
        return commandLineConnection;
    }

    @Override
    public TimeZone getTimeZone() {
        return getProperties().getTimeZone();
    }

    @Override
    public boolean useExtendedCommand() {
        return false;
    }

    @Override
    public int getMaxNrOfRetries() {
        return getProperties().getMaxRetries();
    }

    @Override
    public String getConfiguredSerialNumber() {
        return getOfflineDevice().getSerialNumber();
    }

    @Override
    public boolean useHardCodedInfo() {
        return true;
    }

    @Override
    public boolean useOldProfileFromDate() {
        return false;   // No usage for MK10
    }

    @Override
    public String getProtocolDescription() {
        return "EDMI MK10 [Pull] CommandLine V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2017-22-07 14:27:09 +0100$";
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return null; // Not used
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // Not used
    }

    @Override
    public void logOn() {
        getCommandFactory().enterCommandLineMode();
        getCommandFactory().logon(getProperties().getDeviceId(), getProperties().getPassword());
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void logOff() {
        getCommandFactory().exitCommandLineMode();
    }

    @Override
    public void daisyChainedLogOff() {
        // Don't send the 'Exit' command, or else the modem will be disconnected
    }

    @Override
    public void terminate() {

    }

    @Override
    public String getSerialNumber() {
        return getCommandFactory().getReadCommand(MK10Register.SYSTEM_SERIALNUMBER).getRegister().getString(); // Serial number
    }

    @Override
    public Date getTime() {
        return new TimeInfo(this).getTime();
    }

    @Override
    public void setTime(Date timeToSet) {
        new TimeInfo(this).setTime(timeToSet);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getMk10RegisterFactory().readRegisters(registers);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }


    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Collections.emptyList();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return null;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return null;
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return null;
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId())); // Stand-alone meter without any sub meters
    }

    private MK10RegisterFactory getMk10RegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new MK10RegisterFactory(this);
        }
        return registerFactory;
    }

    public MK10LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new MK10LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    public MK10LogBookFactory getLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new MK10LogBookFactory(this);
        }
        return logBookFactory;
    }

}