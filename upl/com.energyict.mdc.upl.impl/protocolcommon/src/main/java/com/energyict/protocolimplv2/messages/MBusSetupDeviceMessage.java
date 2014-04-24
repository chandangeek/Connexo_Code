package com.energyict.protocolimplv2.messages;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MBusSetupDeviceMessage implements DeviceMessageSpec {

    Decommission(0),
    DataReadout(1),
    Commission(2),
    DecommissionAll(3),
    SetEncryptionKeys(4,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.openKeyAttributeName),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.transferKeyAttributeName)
    ),
    SetEncryptionKeysUsingCryptoserver(5,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.defaultKeyAttributeName)
    ),
    UseCorrectedValues(6),
    UseUncorrectedValues(7);

    private static final DeviceMessageCategory category = DeviceMessageCategories.MBUS_SETUP;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private MBusSetupDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
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
        return MBusSetupDeviceMessage.class.getSimpleName() + "." + this.toString();
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