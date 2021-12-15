package com.energyict.protocolimplv2.nta.dsmr40.landisgyr;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.ip.socket.dsmr.OutboundTcpIpWithWakeUpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.SerialNumberSupport;
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
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40Properties;
import com.energyict.protocolimplv2.nta.dsmr40.common.AbstractSmartDSMR40NtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.eventhandling.Dsmr40LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40Messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class E350 extends AbstractSmartDSMR40NtaProtocol implements SerialNumberSupport {
    private Dsmr40Messaging dsmr40Messaging;
    private Dsmr40MessageExecutor dsmr40MessageExecutor;

    private final NlsService nlsService;
    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;
    private final Converter converter;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final NumberLookupExtractor numberLookupExtractor;
    private final LoadProfileExtractor loadProfileExtractor;
    private Dsmr40LogBookFactory logBookFactory;

    public E350(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, DeviceMessageFileExtractor messageFileExtractor, TariffCalendarExtractor calendarExtractor, NumberLookupExtractor numberLookupExtractor, LoadProfileExtractor loadProfileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
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
    public String getVersion() {
        return "E350 protocol integration version 15.05.2020";
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        journal("LandisGyr E350 protocol init V2");
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        HHUSignOnV2 hhuSignOn = null;
        if (comChannel.getComChannelType() == ComChannelType.SerialComChannel || comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(newDlmsSession(comChannel));
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

        return null;//null ok
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(this.getPropertySpecService()));
        result.add(new OutboundTcpIpWithWakeUpConnectionType(this.getPropertySpecService()));
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr E350 XEMEX DLMS V2 (NTA DSMR4.0)";
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new TcpDeviceProtocolDialect(this.getPropertySpecService(), this.nlsService));
    }

    protected Dsmr40MessageExecutor getMessageExecutor() {
        if (this.dsmr40MessageExecutor == null) {
            this.dsmr40MessageExecutor = new Dsmr40MessageExecutor(this, this.getCollectedDataFactory(), this.getIssueFactory(), this.keyAccessorTypeExtractor);
        }
        return this.dsmr40MessageExecutor;
    }

    protected Dsmr40Messaging getMessaging() {
        if (this.dsmr40Messaging == null) {
            this.dsmr40Messaging = new Dsmr40Messaging(getMessageExecutor(), this.getPropertySpecService(), this.nlsService, this.converter, this.messageFileExtractor, this.calendarExtractor, this.numberLookupExtractor, this.loadProfileExtractor, this.keyAccessorTypeExtractor);
        }
        return this.dsmr40Messaging;
    }

    @Override
    protected LoadProfileBuilder getLoadProfileBuilder(){
        if(this.loadProfileBuilder == null){
            loadProfileBuilder = new LGLoadProfileBuilder(this, this.getCollectedDataFactory(), this.getIssueFactory());
            ((LGLoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(((Dsmr40Properties) getProperties()).getCumulativeCaptureTimeChannel());
        }
        return this.loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return getLogBookFactory().getLogBookData(logBookReaders);
    }

    private Dsmr40LogBookFactory getLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr40LogBookFactory(this, this.getCollectedDataFactory(), this.getIssueFactory());
        }
        return logBookFactory;
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
        return Optional.empty();
    }

    @Override
    protected void checkCacheObjects(){
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        if ((getDeviceCache().getObjectList() == null) || ((Dsmr40Properties) getProperties()).getForcedToReadCache()) {
            journal(((Dsmr40Properties) getProperties()).getForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            journal("Cache exist, will not be read!");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
        }
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return true;
    }
}
