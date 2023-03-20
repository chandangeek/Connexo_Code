package com.energyict.protocolimplv2.dlms.acud.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.GET_GATEWAY_FIRMWARE_VERSION_URL;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_CONFIG_URL;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_FIRMWARE_URL;

public class AcudGatewayConfigurationSupport extends AcudConfigurationSupport {

    public AcudGatewayConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(postGatewayConfigUrlPropertySpec());
        propertySpecs.add(postGatewayFirmwareUrlPropertySpec());
        propertySpecs.add(getGatewayFirmwareVersionUrlPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec postGatewayConfigUrlPropertySpec() {
        return this.stringWithDefaultSpec(POST_GATEWAY_CONFIG_URL, false, PropertyTranslationKeys.V2_POST_GATEWAY_CONFIG_URL, "/");
    }

    protected PropertySpec postGatewayFirmwareUrlPropertySpec() {
        return this.stringWithDefaultSpec(POST_GATEWAY_FIRMWARE_URL, false, PropertyTranslationKeys.V2_POST_GATEWAY_FIRMWARE_URL, "/");
    }

    protected PropertySpec getGatewayFirmwareVersionUrlPropertySpec() {
        return this.stringWithDefaultSpec(GET_GATEWAY_FIRMWARE_VERSION_URL, false, PropertyTranslationKeys.V2_GET_GATEWAY_FIRMWARE_VERSION_URL, "/");
    }
}
