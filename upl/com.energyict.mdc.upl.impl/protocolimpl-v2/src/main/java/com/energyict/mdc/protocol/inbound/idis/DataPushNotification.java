package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InboundDeviceProtocol implementation made for the IDIS DataPush (based on DLMS Data-notification).
 *
 * @author sva
 * @since 13/04/2015 - 16:45
 */
public class DataPushNotification implements BinaryInboundDeviceProtocol {

    protected ComChannel comChannel;
    private InboundDiscoveryContext context;

    private DataPushNotificationParser parser;

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        parser = new DataPushNotificationParser(comChannel, getContext());
        parser.parseInboundFrame();

        return DiscoverResultType.DATA;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        // Nothing to do here - the device doesn't expect an answer from the head-end system
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return parser != null ? parser.getDeviceIdentifier() : null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<>();
        collectedDatas.add(parser.getCollectedRegisters());
        return collectedDatas;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        // No properties
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-07-20 10:41:02 +0300 (Wed, 20 Jul 2016)$";
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }
}
