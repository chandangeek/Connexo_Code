package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifierType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a MessageIdentifier that uniquely identifies a DeviceMessage
 * based on the Device to which it belongs and the messages protocol info.
 *
 * @author sva
 * @since 3/07/13 - 14:34
 */
@XmlRootElement
public class DeviceMessageIdentifierByDeviceAndProtocolInfoParts implements MessageIdentifier {

    private final DeviceIdentifier deviceIdentifier;
    private final String[] messageProtocolInfoParts;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceMessageIdentifierByDeviceAndProtocolInfoParts() {
        deviceIdentifier = null;
        messageProtocolInfoParts = new String[0];
    }

    public DeviceMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts) {
        this.deviceIdentifier = deviceIdentifier;
        this.messageProtocolInfoParts = messageProtocolInfoParts;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        //TODO complete it!
//        List<DeviceMessage> deviceMessages = ManagerFactory.getCurrent().getDeviceMessageFactory().findByDeviceAndProtocolInfoParts(deviceIdentifier.findDevice(), messageProtocolInfoParts);
        List<DeviceMessage> deviceMessages = new ArrayList<>();
        if (deviceMessages.isEmpty()) {
            throw new NotFoundException("DeviceMessage for device (" + deviceIdentifier.toString() + ") and having " + Arrays.toString(messageProtocolInfoParts) + " in protocolInfo not found");
        } else {
            if (deviceMessages.size() > 1) {
                throw new DuplicateException(MessageSeeds.DUPLICATE_FOUND, DeviceMessage.class, this.toString());
            } else {
                return deviceMessages.get(0);
            }
        }
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + deviceIdentifier + " and messageProtocolInfo = " + Arrays.toString(messageProtocolInfoParts);
    }

    @Override
    public MessageIdentifierType getMessageIdentifierType() {
        return MessageIdentifierType.DeviceIdentifierAndProtocolInfoParts;
    }

    @Override
    public List<Object> getIdentifier() {
        return Arrays.asList((Object) getDeviceIdentifier(), getMessageProtocolInfoParts());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifierByDeviceAndProtocolInfoParts that = (DeviceMessageIdentifierByDeviceAndProtocolInfoParts) obj;
        return (this.deviceIdentifier.getIdentifier().equals(that.deviceIdentifier.getIdentifier())) &&
                (this.deviceIdentifier.getDeviceIdentifierType().equals(that.deviceIdentifier.getDeviceIdentifierType())) &&
                Arrays.equals(this.messageProtocolInfoParts, that.messageProtocolInfoParts);
    }

    @Override
    public int hashCode() {
        int result = deviceIdentifier.hashCode();
        result = 31 * result + Arrays.hashCode(messageProtocolInfoParts);
        return result;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String[] getMessageProtocolInfoParts() {
        return messageProtocolInfoParts;
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

}