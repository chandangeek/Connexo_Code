package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the {@link Device} to which it belongs and the messages protocol info.
 *
 * @author sva
 * @since 3/07/13 - 14:34
 */
@XmlRootElement
public class DeviceMessageIdentifierByDeviceAndProtocolInfoParts implements MessageIdentifier {

    private final DeviceIdentifier deviceIdentifier;
    private final String[] messageProtocolInfoParts;

    // For JSON serialization only or in unit tests
    @SuppressWarnings("unused")
    public DeviceMessageIdentifierByDeviceAndProtocolInfoParts() {
        deviceIdentifier = new NullDeviceIdentifier();
        messageProtocolInfoParts = new String[0];
    }

    public DeviceMessageIdentifierByDeviceAndProtocolInfoParts(DeviceIdentifier deviceIdentifier, String... messageProtocolInfoParts) {
        this.deviceIdentifier = deviceIdentifier;
        this.messageProtocolInfoParts = messageProtocolInfoParts;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + deviceIdentifier + " and messageProtocolInfo = " + Arrays.toString(messageProtocolInfoParts);
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
        return this.deviceIdentifier.equals(that.deviceIdentifier)
            && Arrays.equals(this.messageProtocolInfoParts, that.messageProtocolInfoParts);
    }

    @Override
    public int hashCode() {
        int result = deviceIdentifier.hashCode();
        result = 31 * result + Arrays.hashCode(messageProtocolInfoParts);
        return result;
    }

    @XmlAttribute
    public String[] getMessageProtocolInfoParts() {
        return messageProtocolInfoParts;
    }

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

    private static class NullDeviceIdentifier implements DeviceIdentifier {
        @Override
        public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
            return new NullIntrospector();
        }
        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        public void setXmlType(String ignore) {
            // For xml unmarshalling purposes only
        }
    }

    private static class NullIntrospector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Null";
        }

        @Override
        public Set<String> getRoles() {
            return Collections.emptySet();
        }

        @Override
        public Object getValue(String role) {
            throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
        }
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DeviceIdentifierAndProtocolInfoParts";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("device", "protocolInfo"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return getDeviceIdentifier();
                }
                case "protocolInfo": {
                    return getMessageProtocolInfoParts();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }

    }

}