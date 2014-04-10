package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:59
 * Author: khe
 */
@XmlRootElement
public class DeviceMessageIdentifierById implements MessageIdentifier {

    private final int messageId;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceMessageIdentifierById() {
        messageId = -1;
    }

    public DeviceMessageIdentifierById(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        DeviceMessage deviceMessage = MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getDeviceMessageFactory().find(messageId);
        if (deviceMessage == null) {
            throw new NotFoundException("DeviceMessage with messageID " + messageId + " not found");
        }
        return deviceMessage;
    }

    @Override
    public String toString() {
        return "messageId = " + messageId;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public int getMessageId() {
        return messageId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifierById that = (DeviceMessageIdentifierById) obj;
        return this.messageId == that.messageId;
    }

    @Override
    public int hashCode () {
        return messageId;
    }

}
