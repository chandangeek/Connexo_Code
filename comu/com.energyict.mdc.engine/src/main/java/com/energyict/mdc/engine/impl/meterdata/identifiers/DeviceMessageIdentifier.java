package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the ID of the DeviceMessage
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 9:05
 */
public class DeviceMessageIdentifier implements MessageIdentifier {


    private final DeviceMessage<Device> deviceMessage;

    public DeviceMessageIdentifier(DeviceMessage<Device> deviceMessage) {
        this.deviceMessage = deviceMessage;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        return deviceMessage;
    }

    @Override
    public String toString() {
        return "messageId = " + deviceMessage.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifier that = (DeviceMessageIdentifier) obj;
        return this.deviceMessage.getId() == that.getDeviceMessage().getId();
    }

    @Override
    public int hashCode () {
        return (int) this.deviceMessage.getId();
    }

}