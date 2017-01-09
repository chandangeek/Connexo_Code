package com.energyict.protocolimplv2.eict.webrtuz3;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.io.ConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.migration.MigrateFromV1Protocol;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.AbstractMeterTopology;
import com.energyict.protocolimplv2.eict.webrtuz3.loadprofile.LoadProfileBuilder;
import com.energyict.protocolimplv2.eict.webrtuz3.logbooks.LogBookParser;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.WebRTUZ3Messaging;
import com.energyict.protocolimplv2.eict.webrtuz3.properties.WebRTUZ3ConfigurationSupport;
import com.energyict.protocolimplv2.eict.webrtuz3.properties.WebRTUZ3Properties;
import com.energyict.protocolimplv2.eict.webrtuz3.registers.WebRTUZ3RegisterFactory;
import com.energyict.protocolimplv2.eict.webrtuz3.topology.WebRTUZ3MeterTopology;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 22/04/2015 - 16:37
 */
public class WebRTUZ3 extends AbstractDlmsProtocol implements MigrateFromV1Protocol {

    private LoadProfileBuilder loadProfileBuilder;
    private LogBookParser logBookParser;
    private WebRTUZ3RegisterFactory registerFactory;
    private WebRTUZ3Messaging webRTUZ3Messaging;
    private final NlsService nlsService;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;

    public WebRTUZ3(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType());
        result.add(new RxTxAtModemConnectionType());
        result.add(new SioAtModemConnectionType());
        return result;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new WebRTUZ3ConfigurationSupport(this.getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    public DlmsSessionProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new WebRTUZ3Properties();
        }
        return dlmsProperties;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS V2";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookParser().getLogBookData(logBooks);
    }

    public LogBookParser getLogBookParser() {
        if (this.logBookParser == null) {
            this.logBookParser = new LogBookParser(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.logBookParser;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    private WebRTUZ3Messaging getMessaging() {
        if (webRTUZ3Messaging == null) {
            webRTUZ3Messaging = new WebRTUZ3Messaging(this, this.getPropertySpecService(), this.nlsService, this.converter, this.getCollectedDataFactory(), this.getIssueFactory(), calendarExtractor, messageFileExtractor, numberLookupExtractor);
        }
        return webRTUZ3Messaging;
    }

    @Override
    public AbstractMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new WebRTUZ3MeterTopology(this, this.getCollectedDataFactory());
            meterTopology.searchForSlaveDevices();
        }
        return meterTopology;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                    new TcpDeviceProtocolDialect(this.getPropertySpecService()),
                    new SerialDeviceProtocolDialect(this.getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private WebRTUZ3RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new WebRTUZ3RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-05-10 12:20:42 +0200 (Tue, 10 May 2016)$";
    }

    @Override
    public TypedProperties formatLegacyProperties(com.energyict.mdc.upl.properties.TypedProperties legacyProperties) {
        TypedProperties result = TypedProperties.empty();

        // Map 'ServerMacAddress' to 'ServerUpperMacAddress' and 'ServerLowerMacAddress'
        Object serverMacAddress = legacyProperties.getProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        if (serverMacAddress != null) {
            String[] macAddress = ((String) serverMacAddress).split(":");
            if (macAddress.length >= 1) {
                String upperMacAddress = macAddress[0];
                result.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, mapToBigDecimal(upperMacAddress));
            }

            if (macAddress.length >= 2) {
                String lowerMacAddress = macAddress[1];
                result.setProperty(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, mapToBigDecimal(lowerMacAddress));
            }
        }

        return result;
    }

    private BigDecimal mapToBigDecimal(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }
}