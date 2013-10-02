package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ChannelConfigurationDeviceMessage implements DeviceMessageSpec {

    SetFunction(PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getBigDecimalValues()), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetFunctionAttributeName)),
    SetParameters(PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getBigDecimalValues()), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetParametersAttributeName)),
    SetName(PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getBigDecimalValues()), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetNameAttributeName)),
    SetUnit(PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, getBigDecimalValues()), PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUnitAttributeName));

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

    private static final DeviceMessageCategory category = DeviceMessageCategories.CHANNEL_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private ChannelConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return ChannelConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}