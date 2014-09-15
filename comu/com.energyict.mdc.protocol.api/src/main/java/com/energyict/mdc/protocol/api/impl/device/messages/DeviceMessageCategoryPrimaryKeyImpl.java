package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;

/**
 * Serves as a PrimaryKey for a {@link DeviceMessageCategory}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 16:25
 */
public class DeviceMessageCategoryPrimaryKeyImpl extends AbstractDeviceMessagePrimaryKey implements DeviceMessageCategoryPrimaryKey {

    private final DeviceMessageCategories enumValue;
    private final String name;

    public DeviceMessageCategoryPrimaryKeyImpl(DeviceMessageCategories deviceMessageCategoryEnumValue, String name) {
        this.enumValue = deviceMessageCategoryEnumValue;
        this.name = name;
    }

    @Override
    public String getValue() {
        return cleanUpClassName(this.enumValue.getClass().getName()) + CARDINAL_REGEX + this.name;
    }

}