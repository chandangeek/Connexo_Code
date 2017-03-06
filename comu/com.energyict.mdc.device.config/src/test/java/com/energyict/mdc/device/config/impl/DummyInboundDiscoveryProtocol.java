package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

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
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {

    }

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
    public List<CollectedData> getCollectedData() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void copyProperties(TypedProperties properties) {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

}