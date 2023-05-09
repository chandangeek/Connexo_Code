package com.energyict.protocolimplv2.dlms.acud.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.GATEWAY_FIRMWARE_PARTITION_SIZE;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.GATEWAY_FIRMWARE_PARTITION_SIZE_DEFAULT;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.GET_GATEWAY_FIRMWARE_VERSION_URL;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.GET_GATEWAY_FIRMWARE_VERSION_URL_DEFAULT;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_CONFIG_URL;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_CONFIG_URL_DEFAULT;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_FIRMWARE_URL;
import static com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties.POST_GATEWAY_FIRMWARE_URL_DEFAULT;

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
        propertySpecs.add(gatewayFirmwarePartitionSizePropertySpec());
        return propertySpecs;
    }

    protected PropertySpec postGatewayConfigUrlPropertySpec() {
        return this.stringWithDefaultSpec(POST_GATEWAY_CONFIG_URL, false, PropertyTranslationKeys.V2_POST_GATEWAY_CONFIG_URL_PATH, POST_GATEWAY_CONFIG_URL_DEFAULT);
    }

    protected PropertySpec postGatewayFirmwareUrlPropertySpec() {
        return this.stringWithDefaultSpec(POST_GATEWAY_FIRMWARE_URL, false, PropertyTranslationKeys.V2_POST_GATEWAY_FIRMWARE_URL_PATH, POST_GATEWAY_FIRMWARE_URL_DEFAULT);
    }

    protected PropertySpec gatewayFirmwarePartitionSizePropertySpec() {
        return this.stringWithDefaultSpec(GATEWAY_FIRMWARE_PARTITION_SIZE, false, PropertyTranslationKeys.V2_POST_GATEWAY_FIRMWARE_PART_SIZE, GATEWAY_FIRMWARE_PARTITION_SIZE_DEFAULT);
    }

    protected PropertySpec getGatewayFirmwareVersionUrlPropertySpec() {
        return this.stringWithDefaultSpec(GET_GATEWAY_FIRMWARE_VERSION_URL, false, PropertyTranslationKeys.V2_GET_GATEWAY_FIRMWARE_VERSION_URL_PATH, GET_GATEWAY_FIRMWARE_VERSION_URL_DEFAULT);
    }
}
