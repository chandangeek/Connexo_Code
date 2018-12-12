/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.inbound.nfc;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InboundDeviceProtocol implementation made for the NFC DataPush (based on DLMS Data-notification)<br/>
 */
public class NFCDataPushNotification implements BinaryInboundDeviceProtocol {

    protected ComChannel comChannel;
    private InboundDiscoveryContext context;
    private NFCDataPushNotificationParser parser;

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
        parser = new NFCDataPushNotificationParser(comChannel, getContext());
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
    public String getAdditionalInformation() {
        return null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = new ArrayList<>();
        collectedData.add(parser.getCollectedRegisters());
        collectedData.add(parser.getCollectedEvents());
        return collectedData;
    }

    @Override
    public String getVersion() {
        return "$Date: Wed Jul 20 10:41:03 2016 +0300 $";
    }

    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {

    }
}
