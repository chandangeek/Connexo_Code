package com.energyict.protocolimplv2.dlms.ei7;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocolimplv2.dlms.a2.A2Inbound;
import com.energyict.protocolimplv2.dlms.ei7.properties.EI7InboundConfigurationSupport;
import com.energyict.protocolimplv2.dlms.ei7.properties.EI7InboundDlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.util.ArrayList;
import java.util.List;

public class EI7Inbound extends A2Inbound {

    private boolean pushingCompactFrames;
    private DlmsProperties dlmsProperties;
    private EI7DataPushNotificationParser parser;

    public EI7Inbound(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public DiscoverResultType doDiscovery() {
        if (isPushingCompactFrames()) {
            getParser().readAndParseInboundFrame();
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public String getVersion() {
        return "$Date: 2021-11-24$";
    }

    public boolean isPushingCompactFrames() {
        return pushingCompactFrames;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return true;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        if (!isPushingCompactFrames())
            return super.getCollectedData();
        List<CollectedData> collectedDatas = new ArrayList<>();
        if (getParser().getCollectedRegisters().getCollectedRegisters().size() > 0) {
            collectedDatas.add(getParser().getCollectedRegisters());
        }
        if (getParser().getCollectedLoadProfiles().size() > 0) {
            collectedDatas.addAll(getParser().getCollectedLoadProfiles());
        }
        return collectedDatas;
    }

    public EI7DlmsSession createDlmsSession(ComChannel comChannel, DlmsProperties dlmsProperties) {
        return new EI7DlmsSession(comChannel, dlmsProperties, getLogger());
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        if (isPushingCompactFrames())
            return getParser().getDeviceIdentifier();
        return super.getDeviceIdentifier();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        pushingCompactFrames = properties.getTypedProperty(EI7InboundConfigurationSupport.PUSHING_COMPACT_FRAMES, false);
    }

    public EI7InboundDlmsProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new EI7InboundDlmsProperties();
        }
        return (EI7InboundDlmsProperties) dlmsProperties;
    }

    protected HasDynamicProperties getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new EI7InboundConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    protected EI7DataPushNotificationParser getParser() {
        if (parser == null) {
            parser = new EI7DataPushNotificationParser(comChannel, getContext());
        }
        return parser;
    }
}