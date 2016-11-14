package com.energyict.mdc.upl.messages;

/**
 * Serves as a PrimaryKey for a {@link DeviceMessageSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 11:45
 */
public class DeviceMessageSpecPrimaryKey extends AbstractDeviceMessagePrimaryKey {

    private final DeviceMessageSpec deviceMessage;
    private final String name;

    public DeviceMessageSpecPrimaryKey(DeviceMessageSpec deviceMessage, String name) {
        this.deviceMessage = deviceMessage;
        this.name = name;
    }

    /**
     * Provides the primary key for this DeviceMessageSpec.
     *
     * @return the primary key
     */
    public String getValue() {
        return cleanUpClassName(this.deviceMessage.getClass().getName() + CARDINAL_REGEX + this.name);
    }

    public String getName() {
        return name;
    }

}