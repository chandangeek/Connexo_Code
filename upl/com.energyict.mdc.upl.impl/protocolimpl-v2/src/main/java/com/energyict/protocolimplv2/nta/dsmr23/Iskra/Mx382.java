package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.io.ConnectionType;
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
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Mx382 extends AbstractSmartNtaProtocol {
    protected Mx382RegisterFactory registerFactory;
    private Dsmr23Messaging dsmr23Messaging;
    private Dsmr23MessageExecutor dsmr23MessageExecutor;

    private final NlsService nlsService;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;
    public Mx382(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory,
                 NlsService nlsService,KeyAccessorTypeExtractor keyAccessorTypeExtractor,
                 Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor,
                 NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.numberLookupExtractor = numberLookupExtractor;
        this.loadProfileExtractor = loadProfileExtractor;
    }

    protected NlsService getNlsService() {return this.nlsService;}
    protected KeyAccessorTypeExtractor getKeyAccessorTypeExtractor() {return keyAccessorTypeExtractor;}
    protected Converter getConverter () {return converter;}
    protected DeviceMessageFileExtractor getDeviceMessageFileExtractor () {return messageFileExtractor;}
    protected TariffCalendarExtractor getTariffCalendarExtractor () {return calendarExtractor;}
    protected NumberLookupExtractor getNumberLookupExtractor () {return numberLookupExtractor;}
    protected LoadProfileExtractor getLoadProfileExtractor () {return loadProfileExtractor;}

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public String getVersion() {
        return "Mx382 protocol integration version 25.01.2019";
    }

    @Override
    public void journal(String message) {
        super.journal("[Mx382] " + message);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        getLogger().info("Iskra Mx382 protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }

        setDlmsSession(newDlmsSession(comChannel));
        getDlmsSession().getDLMSConnection().setSNRMType(1);//Uses a specific parameter length for the HDLC signon (SNRM request)
    }

    protected HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    protected DlmsSession newDlmsSession(ComChannel comChannel) {
        return new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger());
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
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
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Iskra Mx382 DLMS (NTA DSMR2.3)";
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(this.getPropertySpecService(), this.nlsService));
    }

    protected Dsmr23MessageExecutor getMessageExecutor() {
        if (this.dsmr23MessageExecutor == null) {
            this.dsmr23MessageExecutor = new Dsmr23MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
        }
        return this.dsmr23MessageExecutor;
    }

    private Dsmr23Messaging getDsmr23Messaging() {
        if (this.dsmr23Messaging== null) {
            this.dsmr23Messaging= new Dsmr23Messaging(getMessageExecutor(), this.getPropertySpecService(),
                    this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor,
                    this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.dsmr23Messaging;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessageExecutor().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Mx382RegisterFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return this.registerFactory;
    }
}
