package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;

import com.elster.jupiter.properties.PropertySpec;

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
    public DiscoverResultType doDiscovery() {
        return null;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {

    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return null;
    }

    @Override
    public List<CollectedData> getCollectedData(OfflineDevice device) {
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
        return null;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }
}