package com.energyict.protocolimplv2.dlms.as3000;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.Manufacturer;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractFacadeDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DeviceInformation;
import com.energyict.protocolimplv2.dlms.as3000.custom.ComposedMeterInfo;
import com.energyict.protocolimplv2.dlms.as3000.dlms.AS3000DlmsSession;
import com.energyict.protocolimplv2.dlms.as3000.dlms.AS3000PublicSessionProvider;
import com.energyict.protocolimplv2.dlms.as3000.properties.AS3000ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.as3000.properties.AS3000Properties;
import com.energyict.protocolimplv2.dlms.as3000.readers.AS3000ReadableLoadprofiles;
import com.energyict.protocolimplv2.dlms.as3000.readers.AS3000ReadableLogbook;
import com.energyict.protocolimplv2.dlms.as3000.readers.AS3000ReadableRegister;
import com.energyict.protocolimplv2.dlms.as3000.writers.AS3000Messaging;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounterCache;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AS3000 extends AbstractFacadeDlmsProtocol<FrameCounterCache> {

    private final NlsService nlsService;
    private final Converter converter;
    private HHUSignOnV2 hhuSignOn;

    public AS3000(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, Converter converter) {
        super(propertySpecService, collectedDataFactory, issueFactory, new DeviceInformation(DeviceFunction.METER,
                new ManufacturerInformation(Manufacturer.Elster), "15-10-2020", "AS3000"), new AS3000Properties());
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    protected CollectedLogBookReader<AS3000> getLogBookReader() {
        return new AS3000ReadableLogbook(new CollectedLogBookBuilder(getCollectedDataFactory(), getIssueFactory())).getCollectedLogBookReader(this);
    }

    @Override
    protected CollectedRegisterReader<AS3000> getRegistryReader() {
        return new AS3000ReadableRegister(new CollectedRegisterBuilder(getCollectedDataFactory(), getIssueFactory()), this.getDlmsSessionProperties().getTimeZone()).getRegistryReader(this);
    }

    @Override
    protected CollectedLoadProfileReader<AS3000> getLoadProfileReader() {
        return new AS3000ReadableLoadprofiles(getCollectedDataFactory(), getIssueFactory(), getDlmsSessionProperties().getMaxDaysToReadLoadProfile()).getCollectedLogBookReader(this);
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        return new AS3000Messaging(getCollectedDataFactory(), getIssueFactory(), getPropertySpecService(), this.nlsService, this.converter, this).getMessageHandler();
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        if (comChannel.getComChannelType() == ComChannelType.OpticalComChannel) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        AS3000Properties dlmsSessionProperties = getDlmsSessionProperties();
        super.handleFrameCounter(new AS3000PublicSessionProvider(comChannel, hhuSignOn, getLogger()));
        setDlmsSession(new AS3000DlmsSession(comChannel, dlmsSessionProperties, hhuSignOn, getLogger()));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_MANUFACTURER_SPECIFIC_SEVCD);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new NoParamsDeviceProtocolDialect(nlsService));
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(new OutboundTcpIpConnectionType(getPropertySpecService()),
                new SioOpticalConnectionType(getPropertySpecService()), new RxTxOpticalConnectionType(getPropertySpecService()));
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public AS3000Properties getDlmsSessionProperties() {
        // this is so wrong that cannot be described: see COMMUNICATION-3602
        if (dlmsProperties == null) {
            super.dlmsProperties = new AS3000Properties();
        }
        return (AS3000Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        // this is so wrong that cannot be described: see COMMUNICATION-3602
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AS3000ConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public FrameCounterCache getDeviceCache() {
        // this is so wrong that cannot be described: see COMMUNICATION-3602
        if (super.dlmsCache == null) {
            super.dlmsCache = new FrameCounterCache();
        }
        return (FrameCounterCache) super.dlmsCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof FrameCounterCache) {
            super.dlmsCache = (FrameCounterCache) deviceProtocolCache;
        }
    }

    // this is needed while configuration obis code not present in this meter: 0.0.96.2.0.255.
    // Therefore we need to read it all the time (on logOn)
    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache().getObjectList() == null || getDlmsSessionProperties().flushCachedObjectList()) {
            readObjectList();
            this.getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            // bad model above since meter config is populated in readObjectList. Now we need to populate it here from cache
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
        }
    }

    @Override
    public boolean useDsmr4SelectiveAccessFormat() {
        return true;
    }

    protected ComposedMeterInfo getMeterInfo() {
        return new ComposedMeterInfo(getDlmsSession(),
                getDlmsSessionProperties().isBulkRequest(),
                getDlmsSessionProperties().getRoundTripCorrection(),
                getDlmsSessionProperties().getRetries(), DLMSAttribute.fromString("1:1.1.96.1.0.255:2"), DLMSAttribute.fromString("8:0.0.1.0.0.255:2"));
    }

    @Override
    protected FrameCounterCache buildNewDLMSCache() {
        return new FrameCounterCache();
    }

}
