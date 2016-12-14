package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

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
    private DeviceMessageIdentifierById() {
        messageId = -1;
    }

    public DeviceMessageIdentifierById(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public DeviceMessage getDeviceMessage() {
        DeviceMessage deviceMessage = MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getDeviceMessageFactory().find(messageId);
        if (deviceMessage == null) {
            throw NotFoundException.notFound(DeviceMessage.class, this.toString());
        }
        return deviceMessage;
    }

    @Override
    public String toString() {
        return "messageId = " + messageId;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return getMessageId();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}