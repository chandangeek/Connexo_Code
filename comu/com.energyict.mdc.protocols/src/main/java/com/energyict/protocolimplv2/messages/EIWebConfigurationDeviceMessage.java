package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.protocol.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.dynamic.impl.RequiredPropertySpecFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum EIWebConfigurationDeviceMessage implements DeviceMessageSpec {

    SetEIWebPassword(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebPasswordAttributeName)),
    SetEIWebPage(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebPageAttributeName)),
    SetEIWebFallbackPage(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebFallbackPageAttributeName)),
    SetEIWebSendEvery(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebSendEveryAttributeName)),
    SetEIWebCurrentInterval(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName)),
    SetEIWebDatabaseID(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebDatabaseIDAttributeName)),
    SetEIWebOptions(
            RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            RequiredPropertySpecFactory.newInstance().stringPropertySpec(DeviceMessageConstants.SetEIWebOptionsAttributeName));


    private static final DeviceMessageCategory eiWebCategory = DeviceMessageCategories.EIWEB_PARAMETERS;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private EIWebConfigurationDeviceMessage(PropertySpec... deviceMessagePropertySpecs) {
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return eiWebCategory;
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
        return EIWebConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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
    public
    DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}