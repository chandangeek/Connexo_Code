/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-09 (14:50)
 */
public class DummyInboundDiscoveryProtocol implements InboundDeviceProtocol {


    @Override
    public InboundDiscoveryContext getContext() {
        return null;
    }

    @Override
    public com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResultType doDiscovery() {
        return null;
    }

    @Override
    public void provideResponse(com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResponseType responseType) {

    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return null;
    }

    @Override
    public String getAdditionalInformation() {
        return null;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return null;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void initializeDiscoveryContext(com.energyict.mdc.upl.InboundDiscoveryContext context) {

    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return null;
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {

    }
}