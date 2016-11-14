package com.energyict.mdc.upl.messages;

/**
 * Serves as a PrimaryKey for a {@link DeviceMessageCategory DeviceMessageCategories}
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/02/13
 * Time: 16:25
 */
public class DeviceMessageCategoryPrimaryKey extends AbstractDeviceMessagePrimaryKey {

    private final DeviceMessageCategory deviceMessageCategory;
    private final String name;

    public DeviceMessageCategoryPrimaryKey(DeviceMessageCategory deviceMessageCategory, String name) {
        this.deviceMessageCategory = deviceMessageCategory;
        this.name = name;
    }

    /**
     * Provides the primary key for the given DeviceMessageCategory
     *
     * @return the primary key
     */
    public String getValue() {
        return cleanUpClassName(this.deviceMessageCategory.getClass().getName()) + CARDINAL_REGEX + this.name;
    }

}