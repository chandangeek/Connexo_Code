package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ChannelConfigurationDeviceMessage implements DeviceMessageSpecEnum {

    SetFunction(DeviceMessageId.CHANNEL_CONFIGURATION_SET_FUNCTION, "Set function") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(DeviceMessageConstants.SetFunctionAttributeName, propertySpecService));
        }
    },
    SetParameters(DeviceMessageId.CHANNEL_CONFIGURATION_SET_PARAMETERS, "Set parameters") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(DeviceMessageConstants.SetParametersAttributeName, propertySpecService));
        }
    },
    SetName(DeviceMessageId.CHANNEL_CONFIGURATION_SET_NAME, "Set name") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(DeviceMessageConstants.SetNameAttributeName, propertySpecService));
        }
    },
    SetUnit(DeviceMessageId.CHANNEL_CONFIGURATION_SET_UNIT, "Set unit") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(DeviceMessageConstants.SetUnitAttributeName, propertySpecService));
        }
    },
    SetLPDivisor(DeviceMessageId.CHANNEL_CONFIGURATION_SET_LP_DIVISOR, "Set loadprofile divisor") {
        @Override
        protected void addPropertySpecs(List<PropertySpec> propertySpecs, PropertySpecService propertySpecService) {
            super.addPropertySpecs(propertySpecs, propertySpecService);
            propertySpecs.add(this.stringProperty(DeviceMessageConstants.DivisorAttributeName, propertySpecService));
        }
    };

    /**
     * Return range 1 - 32
     */
    private static BigDecimal[] getBigDecimalValues() {
        BigDecimal[] result = new BigDecimal[32];
        for (int index = 0; index < result.length; index++) {
            result[index] = BigDecimal.valueOf(index + 1);
        }
        return result;
    }

    private DeviceMessageId id;
    private String defaultTranslation;

    ChannelConfigurationDeviceMessage(DeviceMessageId id, String defaultTranslation) {
        this.id = id;
        this.defaultTranslation = defaultTranslation;
    }


    @Override
    public String getKey() {
        return ChannelConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
        propertySpecs.add(propertySpecService.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, true, getBigDecimalValues()));
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