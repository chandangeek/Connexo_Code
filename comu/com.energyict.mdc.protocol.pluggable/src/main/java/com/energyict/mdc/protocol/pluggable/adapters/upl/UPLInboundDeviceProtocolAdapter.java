package com.energyict.mdc.protocol.pluggable.adapters.upl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.exceptions.NestedPropertyValidationException;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * <p>
 * Adapts a given UPL InboundDeviceProtocol to the CXO InboundDeviceProtocol interface
 *
 * @author khe
 * @since 23/02/2017 - 10:58
 */
public class UPLInboundDeviceProtocolAdapter implements InboundDeviceProtocol {

    private final com.energyict.mdc.upl.InboundDeviceProtocol uplInboundDeviceProtocol;

    public UPLInboundDeviceProtocolAdapter(com.energyict.mdc.upl.InboundDeviceProtocol uplInboundDeviceProtocol) {
        this.uplInboundDeviceProtocol = uplInboundDeviceProtocol;
    }

    public com.energyict.mdc.upl.InboundDeviceProtocol getUplInboundDeviceProtocol() {
        return uplInboundDeviceProtocol;
    }

    @Override
    public void initializeDiscoveryContext(com.energyict.mdc.upl.InboundDiscoveryContext context) {
        uplInboundDeviceProtocol.initializeDiscoveryContext(context);
    }

    @Override
    public com.energyict.mdc.upl.InboundDiscoveryContext getContext() {
        return uplInboundDeviceProtocol.getContext();
    }

    @Override
    public com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResultType doDiscovery() {
        return uplInboundDeviceProtocol.doDiscovery();
    }

    @Override
    public void provideResponse(com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResponseType responseType) {
        uplInboundDeviceProtocol.provideResponse(responseType);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return uplInboundDeviceProtocol.getDeviceIdentifier();
    }

    @Override
    public String getAdditionalInformation() {
        return uplInboundDeviceProtocol.getAdditionalInformation();
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return uplInboundDeviceProtocol.getCollectedData();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return uplInboundDeviceProtocol.hasSupportForRequestsOnInbound();
    }

    @Override
    public String getVersion() {
        return uplInboundDeviceProtocol.getVersion();
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        try {
            com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
            uplInboundDeviceProtocol.setUPLProperties(adaptedProperties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return uplInboundDeviceProtocol.getUPLPropertySpecs()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return uplInboundDeviceProtocol.getUPLPropertySpecs();
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) throws PropertyValidationException {
        try {
            com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(properties);
            uplInboundDeviceProtocol.setUPLProperties(adaptedProperties);
        } catch (PropertyValidationException e) {
            throw new NestedPropertyValidationException(e);
        }
    }
}