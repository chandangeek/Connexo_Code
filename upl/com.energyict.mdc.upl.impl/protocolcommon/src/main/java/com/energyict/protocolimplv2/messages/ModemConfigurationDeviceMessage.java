package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum ModemConfigurationDeviceMessage implements DeviceMessageSpec {

    SetDialCommand(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetDialCommandAttributeName)),
    SetModemInit1(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetModemInit1AttributeName)),
    SetModemInit2(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetModemInit2AttributeName)),
    SetModemInit3(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetModemInit3AttributeName)),
    SetPPPBaudRate(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetPPPBaudRateAttributeName)),
    SetModemtype(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetModemtypeAttributeName)),
    SetResetCycle(6, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetResetCycleAttributeName));

    private static final DeviceMessageCategory category = DeviceMessageCategories.MODEM_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private ModemConfigurationDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
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
        return ModemConfigurationDeviceMessage.class.getSimpleName() + "." + this.toString();
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

    @Override
    public int getMessageId() {
        return id;
    }
}