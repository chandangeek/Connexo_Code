package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocolimplv2.DeviceProtocolDialectNameEnum;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of Properties that can be used for FirmwareTesting
 */
public class SDKFirmwareProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String activeMeterFirmwarePropertyName = "ActiveMeterFirmareVersion";
    public static final String passiveMeterFirmwarePropertyName = "PassiveMeterFirmareVersion";
    public static final String activeCommunicationFirmwarePropertyName = "ActiveComFirmareVersion";
    public static final String passiveCommunicationFirmwarePropertyName = "PassiveComFirmareVersion";

    protected SDKFirmwareProtocolDialectProperties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectNameEnum.SDK_SAMPLE_FIRMWARE_DIALECT_NAME.getName();
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect for Firmware testing";
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        switch (name){
            case activeMeterFirmwarePropertyName: return getActiveMeterFirmwareVersionProperty();
            case passiveMeterFirmwarePropertyName: return getPassiveMeterFirmwareVersionProperty();
            case activeCommunicationFirmwarePropertyName: return getActiveCommunicationFirmwareVersionProperty();
            case passiveCommunicationFirmwarePropertyName: return getPassiveCommunicationFirmwareVersionProperty();
            default: return super.getPropertySpec(name);
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> optionalProperties = new ArrayList<>();
        optionalProperties.add(getActiveMeterFirmwareVersionProperty());
        optionalProperties.add(getPassiveMeterFirmwareVersionProperty());
        optionalProperties.add(getActiveCommunicationFirmwareVersionProperty());
        optionalProperties.add(getPassiveCommunicationFirmwareVersionProperty());
        return optionalProperties;
    }

    private PropertySpec getPassiveCommunicationFirmwareVersionProperty() {
        return this.getPropertySpecService().basicPropertySpec(passiveCommunicationFirmwarePropertyName, false, new StringFactory());
    }

    private PropertySpec getActiveCommunicationFirmwareVersionProperty() {
        return this.getPropertySpecService().basicPropertySpec(activeCommunicationFirmwarePropertyName, false, new StringFactory());
    }

    public PropertySpec getActiveMeterFirmwareVersionProperty() {
        return this.getPropertySpecService().basicPropertySpec(activeMeterFirmwarePropertyName, false, new StringFactory());
    }

    public PropertySpec getPassiveMeterFirmwareVersionProperty() {
        return this.getPropertySpecService().basicPropertySpec(passiveMeterFirmwarePropertyName, false, new StringFactory());
    }
}
