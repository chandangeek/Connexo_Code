package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.meterdata.identifiers.MessageIdentifierType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.util.Collections;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the {@link com.energyict.mdw.core.Device} to which it belongs and the messages protocol info.
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
    private DeviceMessageIdentifierByDeviceAndProtocolInfoParts() {
        deviceIdentifier = null;
        messageProtocolInfoParts = new String[0];
    }

    public DeviceMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts) {
        this.deviceIdentifier = deviceIdentifier;
        this.messageProtocolInfoParts = messageProtocolInfoParts;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        List<DeviceMessage> deviceMessages = MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getDeviceMessageFactory().findByDeviceAndProtocolInfoParts(deviceIdentifier.findDevice(), messageProtocolInfoParts);
        if (deviceMessages.isEmpty()) {
            throw new NotFoundException("DeviceMessage for device (" + deviceIdentifier.toString() + ") and having " + Arrays.toString(messageProtocolInfoParts) + " in protocolInfo not found");
        } else {
            if (deviceMessages.size() > 1) {
                throw MdcManager.getComServerExceptionFactory().createDuplicateException(DeviceMessage.class, this.toString());
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
        return Collections.toList((Object) getDeviceIdentifier(), getMessageProtocolInfoParts());
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