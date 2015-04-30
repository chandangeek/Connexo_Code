package com.energyict.mdc.protocol.api.impl.device.messages;

import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 12/8/14
 * Time: 12:08 PM
 */
public enum UplinkConfigurationDeviceMessage implements DeviceMessageSpecEnum {
    EnableUplinkPing(DeviceMessageId.UPLINK_CONFIGURATION_ENABLE_PING, "Enable uplink ping"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(DeviceMessageConstants.enableUplinkPing, true, new BooleanFactory()));
        }
    },
    WriteUplinkPingDestinationAddress(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_DESTINATION_ADDRESS, "Write uplink ping destination address"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.stringPropertySpec(DeviceMessageConstants.uplinkPingDestinationAddress, true, ""));
        }
    },
    WriteUplinkPingInterval(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_INTERVAL, "Write uplink ping interval"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.uplinkPingInterval, true, BigDecimal.ZERO));
        }
    },
    WriteUplinkPingTimeout(DeviceMessageId.UPLINK_CONFIGURATION_WRITE_UPLINK_PING_TIMEOUT, "Write uplink ping timeout"){
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.bigDecimalPropertySpec(DeviceMessageConstants.uplinkPingTimeout, true, BigDecimal.ZERO));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    UplinkConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    public String getKey() {
        return UplinkConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public String getDefaultFormat() {
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
    }

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


