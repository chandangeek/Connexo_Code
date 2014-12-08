package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.*;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.latitudeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.longitudeAttributeName;

/**
 * Copyrights EnergyICT
 * Date: 12/8/14
 * Time: 12:08 PM
 */
public enum UplinkConfigurationDeviceMessage implements DeviceMessageSpecEnum {

//    SET_RELAY_OPERATING_MODE(DeviceMessageId.PUBLIC_LIGHTING_SET_RELAY_OPERATING_MODE, "Public lighting set relay operating mode"){
//        @Override
//        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
//            super.addPropertySpecs(propertySpecs, propertySpecService);
//            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayNumberAttributeName, true, BigDecimal.ONE, BigDecimals.TWO));
//            propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(relayOperatingModeAttributeName, true, BigDecimal.ZERO, BigDecimal.ONE, BigDecimals.TWO, BigDecimals.THREE));
//        }
//    },

    EnableUplinkPing(0, PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.enableUplinkPing)),
    WriteUplinkPingDestinationAddress(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.uplinkPingDestinationAddress)),
    WriteUplinkPingInterval(2, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.uplinkPingInterval)),
    WriteUplinkPingTimeout(3, PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.uplinkPingTimeout));


    ;

    private DeviceMessageId id;
    private String defaultTranslation;

    UplinkConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getNameResourceKey() {
        return UplinkConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String defaultTranslation() {
        return this.defaultTranslation;
    }

    @Override
    public DeviceMessageId getId() {
        return this.id;
    }

    public final List<PropertySpec> getPropertySpecs(PropertySpecService propertySpecService) {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        this.addPropertySpecs(propertySpecs, propertySpecService);
        return propertySpecs;
    }

    protected void addPropertySpecs (List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
        // Default behavior is not to add anything
    };

    @Override
    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}


