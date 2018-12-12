package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import javax.xml.bind.annotation.XmlAttribute;
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

    private final int messageId;
    private final DeviceIdentifier deviceIdentifier;

    // For JSON serialization only  or in unit tests
    @SuppressWarnings("unused")
    public DeviceMessageIdentifierById() {
        messageId = -1;
        deviceIdentifier = null;
    }

    public DeviceMessageIdentifierById(int messageId, DeviceIdentifier deviceIdentifier) {
        this.messageId = messageId;
        this.deviceIdentifier = deviceIdentifier;
    }

    public DeviceMessageIdentifierById(long messageId, DeviceIdentifier deviceIdentifier) {
        this((int) messageId, deviceIdentifier);
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

    @Override
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