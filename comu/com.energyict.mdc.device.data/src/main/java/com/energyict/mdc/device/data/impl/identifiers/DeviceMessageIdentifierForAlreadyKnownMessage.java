package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the ID of the DeviceMessage
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 9:05
 */
public class DeviceMessageIdentifierForAlreadyKnownMessage implements MessageIdentifier {


    private final DeviceMessage<Device> deviceMessage;

    public DeviceMessageIdentifierForAlreadyKnownMessage(DeviceMessage<Device> deviceMessage) {
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
    public List<Object> getIdentifier() {
        return Collections.singletonList(deviceMessage);
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceBySerialNumber(this.deviceMessage.getDevice());
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
        DeviceMessageIdentifierForAlreadyKnownMessage that = (DeviceMessageIdentifierForAlreadyKnownMessage) obj;
        return this.deviceMessage.getId() == that.getDeviceMessage().getId();
    }

    @Override
    public int hashCode () {
        return (int) this.deviceMessage.getId();
    }

}