package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 7/06/13
 * Time: 9:59
 * Author: khe
 */
@XmlRootElement
public class DeviceMessageIdentifierById implements MessageIdentifier {

    private final long messageId;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only  or in unit tests
    @SuppressWarnings("unused")
    public DeviceMessageIdentifierById() {
        messageId = -1;
        deviceIdentifier = null;
    }

    public DeviceMessageIdentifierById(long messageId, DeviceIdentifier deviceIdentifier) {
        this.messageId = messageId;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String toString() {
        return "messageId = " + messageId;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @XmlAttribute
    public long getMessageId() {
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
        return Long.valueOf(messageId).hashCode();
    }

    @Override
    @XmlElements( {
            @XmlElement(type = DeviceIdentifierById.class),
            @XmlElement(type = DeviceIdentifierBySerialNumber.class),
            @XmlElement(type = DeviceIdentifierByMRID.class),
            @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class),
            @XmlElement(type = DeviceIdentifierByDeviceName.class),
            @XmlElement(type = DeviceIdentifierByConnectionTypeAndProperty.class),
    })
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("databaseValue", "device"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue":
                    return getMessageId();
                case "device":
                    return getDeviceIdentifier();
                default:
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}