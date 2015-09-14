package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-20 (13:39)
 */
public class SimpleDiscoveryProtocol implements InboundDeviceProtocol {

    private InboundDiscoveryContext inboundDiscoveryContext;

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.inboundDiscoveryContext = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.inboundDiscoveryContext;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        return DiscoverResultType.IDENTIFIER;
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return mock(DeviceIdentifier.class);
    }

    @Override
    public List<CollectedData> getCollectedData(OfflineDevice device) {
        return Collections.emptyList();
    }

    @Override
    public String getVersion() {
        return "For testing purposes only";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return null;
    }

}