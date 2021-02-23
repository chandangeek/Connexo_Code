package com.energyict.protocolimplv2.dlms.actaris.sl7000;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.Manufacturer;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractFacadeDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DeviceInformation;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.custom.ComposedMeterInfo;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.dlms.ActarisSl7000PublicSessionProvider;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.properties.ActarisSl7000ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.properties.ActarisSl7000Properties;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.ActarisSl7000ReadableLoadprofiles;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.ActarisSl7000ReadableLogbook;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.ActarisSl7000ReadableRegister;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute.billing.StoredValuesImpl;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.writers.ActarisSl7000Messaging;
import com.energyict.protocolimplv2.dlms.common.framecounter.FrameCounterCache;
import com.energyict.protocolimplv2.dlms.common.obis.readers.logbook.CollectedLogBookBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLoadProfileReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedLogBookReader;
import com.energyict.protocolimplv2.dlms.common.readers.CollectedRegisterReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class ActarisSl7000 extends AbstractFacadeDlmsProtocol<FrameCounterCache> {

    private final NlsService nlsService;
    private final DeviceMessageFileExtractor deviceMessageFileExtractor;
    private final Converter converter;
    private StoredValuesImpl storedValues;

    public ActarisSl7000(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, NlsService nlsService, DeviceMessageFileExtractor deviceMessageFileExtractor, Converter converter) {
        super(propertySpecService, collectedDataFactory, issueFactory, new DeviceInformation(DeviceFunction.METER,
                new ManufacturerInformation(Manufacturer.Actaris), "22-02-2021", "ActarisSl7000"), new ActarisSl7000Properties());
        this.nlsService = nlsService;
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
        this.converter = converter;
    }

    @Override
    protected CollectedLogBookReader<ActarisSl7000> getLogBookReader() {
        return new ActarisSl7000ReadableLogbook(new CollectedLogBookBuilder(getCollectedDataFactory(), getIssueFactory())).getCollectedLogBookReader(this);
    }

    @Override
    protected CollectedRegisterReader<ActarisSl7000> getRegistryReader() {
        return new ActarisSl7000ReadableRegister(new CollectedRegisterBuilder(getCollectedDataFactory(), getIssueFactory()), this).getRegistryReader(this);
    }

    @Override
    protected CollectedLoadProfileReader<ActarisSl7000> getLoadProfileReader() {
        return new ActarisSl7000ReadableLoadprofiles(getCollectedDataFactory(), getIssueFactory(), getDlmsSessionProperties().getLimitMaxNrOfDays()).getCollectedLogBookReader(this);
    }

    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        return new ActarisSl7000Messaging(getCollectedDataFactory(), getIssueFactory(), getPropertySpecService(), nlsService, converter, this, deviceMessageFileExtractor).getMessageHandler();
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        // Loop over all registers to determine the highest billingPoint & request billingPointDateTime for highest point
        // This will ensure all necessary billing points are present in the  profile buffer
        int billingPoint = -1;
        for (OfflineRegister reg : registers) {
            int f = reg.getObisCode().getF();
            if (f != 255 && f > billingPoint) {
                billingPoint = f;
            }
        }
        try {
            if (billingPoint != -1 && billingPoint <= getStoredValues().getBillingPointCounter()) {
                getStoredValues().getBillingPointTimeDate(billingPoint);
            }
        } catch (IOException e) {
            journal(Level.SEVERE, "Problems while reading historical billingPoint " + billingPoint);
        }
        return super.readRegisters(registers);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        super.handleFrameCounter(new ActarisSl7000PublicSessionProvider(comChannel, getLogger()));
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), getLogger()));
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<? extends ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new SioAtModemConnectionType(this.getPropertySpecService()),
                new RxTxAtModemConnectionType(this.getPropertySpecService()),
                new OutboundTcpIpConnectionType(this.getPropertySpecService())
        );
    }

    @Override
    public List<? extends DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Collections.singletonList(new NoParamsDeviceProtocolDialect(nlsService));
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    public StoredValuesImpl getStoredValues() {
        if (storedValues == null) {
            storedValues = new StoredValuesImpl(this);
        }
        return storedValues;
    }

    @Override
    public ActarisSl7000Properties getDlmsSessionProperties() {
        // this is so wrong that cannot be described: see COMMUNICATION-3602
        if (dlmsProperties == null) {
            super.dlmsProperties = new ActarisSl7000Properties();
        }
        return (ActarisSl7000Properties) dlmsProperties;
    }

    @Override
    protected HasDynamicProperties getDlmsConfigurationSupport() {
        // this is so wrong that cannot be described: see COMMUNICATION-3602
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new ActarisSl7000ConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    @Override
    protected ComposedMeterInfo getMeterInfo() {
        return new ComposedMeterInfo(getDlmsSession(), getDlmsSessionProperties().isBulkRequest(), getDlmsSessionProperties().getRoundTripCorrection(), getDlmsSessionProperties().getRetries());
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

    @Override
    public void logOff() {
        getDlmsSession().getDlmsV2Connection().disconnectMAC();
    }

    @Override
    protected FrameCounterCache buildNewDLMSCache() {
        return new FrameCounterCache();
    }

}
