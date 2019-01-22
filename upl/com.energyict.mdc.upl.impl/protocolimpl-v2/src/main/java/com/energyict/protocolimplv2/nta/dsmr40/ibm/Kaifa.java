package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40Properties;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.E350;
import com.energyict.protocolimplv2.nta.dsmr40.messages.KaifaDsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.KaifaDsmr40Messaging;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Kaifa extends E350 {
    private KaifaDsmr40Messaging kaifaMessaging;
    private KaifaDsmr40MessageExecutor kaifaMessageExecutor;

    private final NlsService nlsService;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;

    public Kaifa(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory, nlsService, converter, messageFileExtractor, calendarExtractor, numberLookupExtractor, loadProfileExtractor, keyAccessorTypeExtractor);
        this.nlsService = nlsService;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    @Override
    public String getVersion() {
        return "Kaifa protocol integration version 17.01.2019";
    }

    @Override
    public String getProtocolDescription() {
        return "Kaifa DLMS (NTA DSMR4.0)";
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("Kaifa protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, ""));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new KaifaHHUSignon(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            byte[] firmwareVersion = getMeterInfo().getFirmwareVersion().getBytes();
            return ProtocolTools.getHexStringFromBytes(firmwareVersion);
        } catch (CommunicationException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "Unknown version";
        }
    }

    protected KaifaDsmr40MessageExecutor getMessageExecutor() {
    if (this.kaifaMessageExecutor == null) {
        this.kaifaMessageExecutor = new KaifaDsmr40MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
    }
    return this.kaifaMessageExecutor;
}

    protected KaifaDsmr40Messaging getKaifaDSMR40Messaging() {
        if (this.kaifaMessaging == null) {
            this.kaifaMessaging = new KaifaDsmr40Messaging(getMessageExecutor(), this.getPropertySpecService(),
                    this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor,
                    this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.kaifaMessaging;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {

        return getKaifaDSMR40Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getKaifaDSMR40Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getKaifaDSMR40Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getKaifaDSMR40Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

//
//    @Override
//    public DlmsProtocolProperties getProperties() {
//        if (this.properties == null) {
//            this.properties = new KaifaProperties();
//        }
//        return this.properties;
//    }
//

    @Override
    public DlmsProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr40Properties();
        }
        return this.properties;
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new KaifaRegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.registerFactory;
    }

}
