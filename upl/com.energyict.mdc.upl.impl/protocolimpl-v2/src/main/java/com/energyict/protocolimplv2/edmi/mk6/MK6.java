/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6;

import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.journal.ProtocolJournal;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimpl.edmi.common.connection.CommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.ExtendedCommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.MiniECommandLineConnection;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;
import com.energyict.protocolimplv2.edmi.dialects.CommonEDMIDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.ModemDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.UdpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.mk6.events.MK6LogBookFactory;
import com.energyict.protocolimplv2.edmi.mk6.profiles.ExtensionFactory;
import com.energyict.protocolimplv2.edmi.mk6.profiles.MK6LoadProfileBuilder;
import com.energyict.protocolimplv2.edmi.mk6.properties.MK6ConfigurationSupport;
import com.energyict.protocolimplv2.edmi.mk6.properties.MK6Properties;
import com.energyict.protocolimplv2.edmi.mk6.registers.MK6RegisterFactory;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.security.SimplePasswordSecuritySupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * @author sva
 * @since 3/03/2017 - 16:50
 */
public class MK6 implements DeviceProtocol, CommandLineProtocol {

    private ComChannel comChannel;
    private OfflineDevice offlineDevice;
    private MK6ConfigurationSupport configurationSupport;
    private SimplePasswordSecuritySupport securitySupport;
    private CommandLineConnection commandLineConnection;
    private CommandFactory commandFactory;
    private ExtensionFactory extensionFactory;
    private MK6Properties properties;
    private MK6LogBookFactory logBookFactory;
    private MK6LoadProfileBuilder loadProfileBuilder;
    private MK6RegisterFactory registerFactory;
    private final PropertySpecService propertySpecService;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final NlsService nlsService;
    private ProtocolJournal protocolJournal;

    public MK6(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.nlsService = nlsService;
    }

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
    public List<PropertySpec> getUPLPropertySpecs() {
        return getConfigurationSupport().getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        getProperties().addProperties(properties);
    }

    protected MK6ConfigurationSupport getConfigurationSupport() {
        if (configurationSupport == null) {
            configurationSupport = new MK6ConfigurationSupport(propertySpecService);
        }
        return configurationSupport;
    }

    public MK6Properties getProperties() {
        if (properties == null) {
            properties = new MK6Properties();
        }
        return properties;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundUdpConnectionType(propertySpecService));
        result.add(new OutboundTcpIpConnectionType(propertySpecService));
        result.add(new InboundIpConnectionType());
        result.add(new SioAtModemConnectionType(propertySpecService));
        result.add(new RxTxAtModemConnectionType(propertySpecService));
        return result;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> result = new ArrayList<>();
        result.add(new TcpDeviceProtocolDialect(propertySpecService, this.nlsService));
        result.add(new UdpDeviceProtocolDialect(propertySpecService, this.nlsService));
        result.add(new ModemDeviceProtocolDialect(propertySpecService, this.nlsService));
        return result;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getProperties().addProperties(dialectProperties);
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
    public Optional<PropertySpec> getClientSecurityPropertySpec() {
        return getSecuritySupport().getClientSecurityPropertySpec();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return getSecuritySupport().getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return getSecuritySupport().getEncryptionAccessLevels();
    }

    public SimplePasswordSecuritySupport getSecuritySupport() {
        if (this.securitySupport == null) {
            this.securitySupport = new SimplePasswordSecuritySupport(propertySpecService);
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

    public ExtensionFactory getExtensionFactory() {
        if (extensionFactory == null) {
            extensionFactory = new ExtensionFactory(getCommandFactory());
        }
        return extensionFactory;
    }

    @Override
    public CommandLineConnection getCommandLineConnection() {
        if (commandLineConnection == null) {
            CommonEDMIDeviceProtocolDialect.ConnectionMode connectionMode = getProperties().getConnectionMode();
            switch (connectionMode) {
                case UNDEFINED: journal("No Connection Mode specified on Dialect, Default to Extended command line");
                case EXTENDED_COMMAND_LINE: commandLineConnection = new ExtendedCommandLineConnection(
                        new ComChannelInputStreamAdapter(getComChannel()),
                        new ComChannelOutputStreamAdapter(getComChannel()),
                        getProperties().getTimeout(),
                        getProperties().getMaxRetries(),
                        getProperties().getForcedDelay(),
                        0,
                        null,
                        getOfflineDevice().getSerialNumber());
                    break;
                case MINI_E_COMMAND_LINE: commandLineConnection= new MiniECommandLineConnection(
                        new ComChannelInputStreamAdapter(getComChannel()),
                        new ComChannelOutputStreamAdapter(getComChannel()),
                        getProperties().getTimeout(),
                        getProperties().getMaxRetries(),
                        getProperties().getForcedDelay(),
                        0,
                        null,
                        getOfflineDevice().getSerialNumber());
                    break;
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
        return true;
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
    public boolean isMK10() {
        return false;
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
        return "EDMI MK6 CommandLine V2";
    }

    @Override
    public String getVersion() {
        return "$Date: 2020-07-23$";
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
        getCommandFactory().logon(getProperties().getDeviceId(), getProperties().getPassword());
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
        return getCommandFactory().getReadCommand(MK6RegisterInformation.SYSTEM_SERIAL_NUMBER).getRegister().getString(); // Serial number
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
        return getMk6RegisterFactory().readRegisters(registers);
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
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId())); // Stand-alone meter without any sub meters
    }

    private MK6RegisterFactory getMk6RegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new MK6RegisterFactory(this, collectedDataFactory, issueFactory);
        }
        return registerFactory;
    }

    public MK6LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new MK6LoadProfileBuilder(this, collectedDataFactory, issueFactory, offlineDevice);
        }
        return loadProfileBuilder;
    }

    public MK6LogBookFactory getLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new MK6LogBookFactory(this, collectedDataFactory, issueFactory);
        }
        return logBookFactory;
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return collectedDataFactory.createCalendarCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return collectedDataFactory.createBreakerStatusCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public CollectedCreditAmount getCreditAmount() {
        return this.collectedDataFactory.createCreditAmountCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return collectedDataFactory.createFirmwareVersionsCollectedData(offlineDevice.getDeviceIdentifier());
    }

    @Override
    public void setProtocolJournaling(ProtocolJournal protocolJournal) {
        this.protocolJournal = protocolJournal;
    }

    @Override
    public void journal(String message) {
        if (protocolJournal!=null) {
            protocolJournal.addToJournal(message);
        }
    }
}