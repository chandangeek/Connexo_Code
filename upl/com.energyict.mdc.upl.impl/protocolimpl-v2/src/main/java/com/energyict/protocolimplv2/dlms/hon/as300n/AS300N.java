package com.energyict.protocolimplv2.dlms.hon.as300n;

import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.direct.serialio.SioSerialConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.*;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.hon.as300n.dlms.AS300NFrameCounterHandler;
import com.energyict.protocolimplv2.dlms.hon.as300n.messages.AS300NMessaging;
import com.energyict.protocolimplv2.dlms.hon.as300n.profile.AS300NProfileDataReader;
import com.energyict.protocolimplv2.dlms.hon.as300n.properties.AS300NConfigurationSupport;
import com.energyict.protocolimplv2.dlms.hon.as300n.properties.AS300NProperties;
import com.energyict.protocolimplv2.dlms.hon.as300n.registers.AS300NRegisterFactory;
import com.energyict.protocolimplv2.dlms.hon.as300n.registers.AS300NStoredValues;

import java.util.*;

public class AS300N extends AbstractDlmsProtocol implements SerialNumberSupport {


    private final TariffCalendarExtractor calendarExtractor;
    private final Converter converter;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final PropertySpecService propertySpecService;
    private NlsService nlsService;

    private AS300NMessaging messagingSupport;
    private AS300NStoredValues storedValues;
    private AS300NProfileDataReader profileDataReader;
    private AS300NRegisterFactory registerFactory;
    private AS300NFrameCounterHandler frameCounterHandler;
    private AS300NProperties    as300NProperties;


    public AS300N(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.propertySpecService = propertySpecService;
        this.calendarExtractor = calendarExtractor;
        this.nlsService = nlsService;
        this.converter = converter;
        this.messageFileExtractor = messageFileExtractor;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell AS300N DLMS";
    }

    @Override
    public String getVersion() {
        return "2019-07-04";
    }

    @Override
    public void journal(String message){
        super.journal("[AS300N] "+message);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        journal("Start protocol for " + offlineDevice.getSerialNumber() + " version "+getVersion());
        this.frameCounterHandler = new AS300NFrameCounterHandler(this, comChannel);
        frameCounterHandler.handleFrameCounter();
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));
        journal("Protocol initialization phase ended, executing tasks ...");
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(   DeviceProtocolCapabilities.PROTOCOL_MASTER,
                                DeviceProtocolCapabilities.PROTOCOL_SESSION);

    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.METER;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new InboundIpConnectionType());
        result.add(new SioOpticalConnectionType(this.getPropertySpecService()));
        result.add(new SioAtModemConnectionType(this.getPropertySpecService()));
        result.add(new SioSerialConnectionType(this.getPropertySpecService()));

        return result;
    }


    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.asList(
                (DeviceProtocolDialect) new SerialDeviceProtocolDialect(this.getPropertySpecService(), getNlsService()), // HDLC.
                new TcpDeviceProtocolDialect(this.getPropertySpecService(), getNlsService())   // GPRS modem.
        );


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
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return Collections.emptyList();
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
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getMessaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getAS300NRegisterFactory().readRegisters(registers);
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AS300NConfigurationSupport(this.propertySpecService);
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public Date getTime() {
        if (getDlmsSessionProperties().useBeaconMirrorDeviceDialect()) {
            return new Date();  //Don't read out the clock of the mirror logical device, it does not know the actual meter time.
        } else {
            Date actualMeterTime = super.getTime();
            TimeZone configuredTimeZone = getDlmsSessionProperties().getTimeZone();
            Calendar adjustedMeterTime = Calendar.getInstance(configuredTimeZone);
            adjustedMeterTime.setTime(actualMeterTime);
            return adjustedMeterTime.getTime();
        }
    }




    /**
     * Specific implementation functions
     *
     * @return
     */

    public AS300NProperties getDlmsSessionProperties() {
        if (as300NProperties == null) {
            as300NProperties = new AS300NProperties();
        }
        return as300NProperties;
    }

    private AS300NRegisterFactory getAS300NRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new AS300NRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return registerFactory;
    }


    public NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }

    protected TariffCalendarExtractor getCalendarExtractor() {
        return calendarExtractor;
    }

    protected DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {
        return keyAccessorTypeExtractor;
    }

    public AS300NMessaging getMessaging() {
        if (messagingSupport == null) {
            messagingSupport = new AS300NMessaging(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.getPropertySpecService(), this.nlsService, this.converter, this.calendarExtractor, this.messageFileExtractor, this.keyAccessorTypeExtractor);
        }
        return messagingSupport;
    }

    public StoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new AS300NStoredValues(this);
        }
        return storedValues;
    }

    public AS300NProfileDataReader getProfileDataReader() {
        if (profileDataReader == null) {
            profileDataReader = new AS300NProfileDataReader(this, getDlmsSessionProperties().getLimitMaxNrOfDays(), this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return profileDataReader;
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return true;
    }
}