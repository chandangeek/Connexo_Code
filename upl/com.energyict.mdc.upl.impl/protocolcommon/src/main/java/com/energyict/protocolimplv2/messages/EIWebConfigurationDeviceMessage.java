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
public enum EIWebConfigurationDeviceMessage implements DeviceMessageSpec {

    SetEIWebPassword(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebPasswordAttributeName)),
    SetEIWebPage(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebPageAttributeName)),
    SetEIWebFallbackPage(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebFallbackPageAttributeName)),
    SetEIWebSendEvery(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebSendEveryAttributeName)),
    SetEIWebCurrentInterval(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebCurrentIntervalAttributeName)),
    SetEIWebDatabaseID(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebDatabaseIDAttributeName)),
    SetEIWebOptions(
            PropertySpecFactory.bigDecimalPropertySpecWithValues(DeviceMessageConstants.id, BigDecimal.valueOf(1), BigDecimal.valueOf(2)),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetEIWebOptionsAttributeName));


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
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }
}