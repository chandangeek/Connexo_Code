package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.cim.EndDeviceType;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
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
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.*;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.acud.messages.AcudMessaging;
import com.energyict.protocolimplv2.dlms.acud.profiledata.AcudLoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.acud.properties.AcudConfigurationSupport;
import com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;

import java.io.IOException;
import java.util.*;

public abstract class Acud extends AbstractDlmsProtocol {

    /**
     * Predefiened obis codes for the Acud meter
     */
    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    private static final ObisCode FIRMWARE_VERSION_OBIS_CODE = ObisCode.fromString("0.0.0.2.0.255");

    private HHUSignOnV2 hhuSignOnV2;
    private AcudRegisterFactory registerFactory;
    private AcudLogBookFactory logBookFactory;
    private AcudLoadProfileDataReader loadProfileDataReader;

    private final Converter converter;
    private final NlsService nlsService;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;

    private AcudMessaging messaging;

    public Acud(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
        this.converter = converter;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        if (comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOnV2 = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new AcudDlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOnV2, getDlmsSessionProperties().getDeviceId()));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(getPropertySpecService()),
                new RxTxOpticalConnectionType(getPropertySpecService()),
                new OutboundTcpIpConnectionType(getPropertySpecService()));
    }

    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if (dlmsCache.getObjectList() == null || readCache) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            UniversalObject[] objectList = dlmsCache.getObjectList();
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(objectList);
        }
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileDataReader().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileDataReader().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    public AcudLoadProfileDataReader getLoadProfileDataReader() {
        if (this.loadProfileDataReader == null) {
            this.loadProfileDataReader = new AcudLoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(), offlineDevice);
        }
        return this.loadProfileDataReader;
    }

    public AcudRegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = createRegisterFactory();
        }
        return this.registerFactory;
    }

    protected AcudRegisterFactory createRegisterFactory() {
        return new AcudRegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
    }

    public AcudLogBookFactory getLogBookFactory() {
        if (this.logBookFactory == null) {
            this.logBookFactory = createLogBookFactory();
        }
        return this.logBookFactory;
    }

    protected abstract AcudLogBookFactory createLogBookFactory();

    @Override
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    public String getFirmwareVersion() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(FIRMWARE_VERSION_OBIS_CODE).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    public AcudDlmsProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AcudDlmsProperties();
        }
        return (AcudDlmsProperties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AcudConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    protected AcudMessaging getProtocolMessaging() {
        if (messaging == null) {
            messaging = createProtocolMessaging();
        }
        return messaging;
    }

    protected AcudMessaging createProtocolMessaging() {
        return new AcudMessaging(this, getPropertySpecService(), getNlsService(), getConverter(), getTariffCalendarExtractor(), getMessageFileExtractor());
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getProtocolMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getProtocolMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getProtocolMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        return getProtocolMessaging().format(offlineDevice, offlineDeviceMessage, propertySpec, messageAttribute);
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return getProtocolMessaging().prepareMessageContext(device, offlineDevice, deviceMessage);
    }

    public abstract EndDeviceType getTypeMeter();

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new NoParamsDeviceProtocolDialect(getNlsService()));
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions(String serialNumber) {
        if (offlineDevice.getSerialNumber().equals(serialNumber)) {
            CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(new DeviceIdentifierById(offlineDevice.getId()));
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(getFirmwareVersion());
            return firmwareVersionsCollectedData;
        }
        return super.getFirmwareVersions(serialNumber);
    }

    private CollectedCreditAmount getCreditAmount( CreditDeviceMessage.CreditType credit_t) {
        CollectedCreditAmount creditAmountCollectedData = super.getCreditAmount();
        getRegisterFactory().readCreditAmount(creditAmountCollectedData, credit_t);
        return creditAmountCollectedData;
    }

    @Override
    public List<CollectedCreditAmount> getCreditAmounts() {
        List<CollectedCreditAmount> cda = new ArrayList<>();
        cda.add(getCreditAmount(CreditDeviceMessage.CreditType.Emergency_credit));
        cda.add(getCreditAmount(CreditDeviceMessage.CreditType.Import_credit));
        return cda;
    }

    public TariffCalendarExtractor getTariffCalendarExtractor() {
        return calendarExtractor;
    }


    public DeviceMessageFileExtractor getMessageFileExtractor() {
        return messageFileExtractor;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }

    protected Converter getConverter() {
        return converter;
    }
}