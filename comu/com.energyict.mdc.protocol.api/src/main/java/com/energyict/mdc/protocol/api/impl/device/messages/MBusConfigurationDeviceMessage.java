package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.util.ArrayList;
import java.util.List;

import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMBusConfigAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMBusEveryAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMBusInterFrameTimeAttributeName;
import static com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants.SetMBusVIFAttributeName;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MBusConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetMBusEvery(DeviceMessageId.MBUS_CONFIGURATION_SET_EVERY, "Set MBus every") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMBusEveryAttributeName, propertySpecService));
        }
    },
    SetMBusInterFrameTime(DeviceMessageId.MBUS_CONFIGURATION_SET_INTER_FRAME_TIME, "Set MBus inter frame time") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMBusInterFrameTimeAttributeName, propertySpecService));
        }
    },
    SetMBusConfig(DeviceMessageId.MBUS_CONFIGURATION_SET_CONFIG, "Set MBus configuration") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(SetMBusConfigAttributeName, propertySpecService));
        }
    },
    SetMBusVIF(DeviceMessageId.MBUS_CONFIGURATION_SET_VIF, "Set MBus VIF") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(propertySpecService.basicPropertySpec(SetMBusVIFAttributeName, true, new HexStringFactory()));
        }
    };

    private DeviceMessageId id;
    private String defaultTranslation;

    MBusConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }

    @Override
    public String getKey() {
        return MBusConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    };

    protected PropertySpec stringProperty(String name, PropertySpecService propertySpecService) {
        return propertySpecService.basicPropertySpec(name, true, new StringFactory());
    }

    public final PropertySpec getPropertySpec(String name, PropertySpecService propertySpecService) {
        for (PropertySpec securityProperty : getPropertySpecs(propertySpecService)) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

}