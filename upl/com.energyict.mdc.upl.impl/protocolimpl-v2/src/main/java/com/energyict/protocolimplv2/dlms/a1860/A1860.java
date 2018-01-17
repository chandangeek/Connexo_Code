package com.energyict.protocolimplv2.dlms.a1860;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
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
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a1860.events.A1860LogBookFactory;
import com.energyict.protocolimplv2.dlms.a1860.profiledata.A1860LoadProfileDataReader;
import com.energyict.protocolimplv2.dlms.a1860.properties.A1860ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.a1860.properties.A1860Properties;
import com.energyict.protocolimplv2.dlms.a1860.registers.A1860RegisterFactory;
import com.energyict.protocolimplv2.dlms.a1860.registers.A1860StoredValues;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class A1860 extends AbstractDlmsProtocol {

    private A1860RegisterFactory registerFactory;
    private A1860LoadProfileDataReader loadProfileDataReader;
    private A1860LogBookFactory logBookFactory;
    private A1860StoredValues storedValues;
    private HHUSignOnV2 hhuSignOn;
    private OfflineDevice offlineDevice;
    private final NlsService nlsService;

    public A1860(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService) {
        super(propertySpecService, collectedDataFactory, issueFactory);
        this.nlsService = nlsService;
    }

    private static final ObisCode SERIAL_NUMBER_OBISCODE = ObisCode.fromString("1.0.96.1.0.255");

    @Override
    public String getVersion() {
        return "$Date: 2017-07-12 11:00:11 +0200 (Wed, 12 Jul 2017)$";
    }

    @Override
    public String getProtocolDescription() {
        return "Elster Alpha A1860 DLMS";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new NoParamsDeviceProtocolDialect(getNlsService()));
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioOpticalConnectionType(getPropertySpecService()),
                new RxTxOpticalConnectionType(getPropertySpecService()),
                new OutboundTcpIpConnectionType(getPropertySpecService()));
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getA1860RegisterFactory().readRegisters(registers);
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
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        if (comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }

        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new A1860DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, getDlmsSessionProperties().getDeviceId()));
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
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
    public String getSerialNumber() {
        try {
            return getDlmsSession().getCosemObjectFactory().getData(SERIAL_NUMBER_OBISCODE).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getDlmsSession().getProperties().getRetries() + 1);
        }
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = getDeviceCache();
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if (dlmsCache.getObjectList() == null || readCache) {
            getDlmsSession().getLogger().info("Reading cache from device");
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    @Override
    public A1860Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new A1860Properties();
        }
        return (A1860Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new A1860ConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    public StoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new A1860StoredValues(this);
        }
        return storedValues;
    }

    public A1860LoadProfileDataReader getLoadProfileDataReader(){
        if(loadProfileDataReader == null){
            loadProfileDataReader = new A1860LoadProfileDataReader(this, getCollectedDataFactory(), getIssueFactory(), offlineDevice);
            return loadProfileDataReader;
        } else {
            return loadProfileDataReader;
        }
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private A1860LogBookFactory getLogBookFactory(){
        if(logBookFactory == null){
            logBookFactory = new A1860LogBookFactory(this, getCollectedDataFactory(), getIssueFactory());
            return logBookFactory;
        }else {
            return logBookFactory;
        }
    }

    private A1860RegisterFactory getA1860RegisterFactory(){
        if(registerFactory == null){
            registerFactory = new A1860RegisterFactory(this, getCollectedDataFactory(), getIssueFactory());
        }
        return registerFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>();
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
        return null;
    }

    public NlsService getNlsService() {
        return nlsService;
    }
}
