package com.energyict.mdc.protocol.api.impl.device.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;

/**
 * Serves as a PrimaryKey for a {@link DeviceMessageSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 7/02/13
 * Time: 11:45
 */
public class DeviceMessageSpecPrimaryKeyImpl extends AbstractDeviceMessagePrimaryKey implements DeviceMessageSpecPrimaryKey {

    private final DeviceMessageSpecEnum deviceMessage;
    private final String name;

    public DeviceMessageSpecPrimaryKeyImpl(DeviceMessageSpecEnum deviceMessage, String name) {
        this.deviceMessage = deviceMessage;
        this.name = name;
    }

    @Override
    public String getValue() {
        return cleanUpClassName(this.deviceMessage.getClass().getName() + CARDINAL_REGEX + this.name);
    }

    @Override
    public String getName() {
        return name;
    }

}