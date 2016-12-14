package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifierType;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the ID of the DeviceMessage
 * <p>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 9:05
 */
public class DeviceMessageIdentifierForAlreadyKnownMessage implements MessageIdentifier {

    private final DeviceMessage deviceMessage;

    public DeviceMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage) {
        this.deviceMessage = deviceMessage;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        return deviceMessage;
    }

    @Override
    public MessageIdentifierType getMessageIdentifierType() {
        return MessageIdentifierType.ActualMessage;
    }

    @Override
    public List<Object> getParts() {
        return Collections.singletonList(deviceMessage);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(this.deviceMessage.getDevice());
    }

    @Override
    public String toString() {
        return "message having id " + deviceMessage.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifierForAlreadyKnownMessage that = (DeviceMessageIdentifierForAlreadyKnownMessage) obj;
        return this.deviceMessage.getId() == that.getDeviceMessage().getId();
    }

    @Override
    public int hashCode() {
        return (int) this.deviceMessage.getId();
    }
}