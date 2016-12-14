package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.protocol.exceptions.identifier.DuplicateException;
import com.energyict.protocol.exceptions.identifier.NotFoundException;

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
        deviceIdentifier = new NullDeviceIdentifier();
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
            throw NotFoundException.notFound(DeviceMessage.class, this.toString());
        } else {
            if (deviceMessages.size() > 1) {
                throw DuplicateException.duplicateFoundFor(DeviceMessage.class, this.toString());
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
        return (this.deviceIdentifier.equals(that.deviceIdentifier))
                && Arrays.equals(this.messageProtocolInfoParts, that.messageProtocolInfoParts);
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

    private static class NullDeviceIdentifier implements DeviceIdentifier {
        @Override
        public Device findDevice() {
            throw new UnsupportedOperationException("NullDeviceIdentifier is not capable of finding a device because there is not identifier");
        }

        @XmlElement(name = "type")
        public String getXmlType() {
            return this.getClass().getName();
        }

        public void setXmlType(String ignore) {
            // For xml unmarshalling purposes only
        }

        @Override
        public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
            return new NullIntrospector();
        }
    }

    private static class NullIntrospector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Null";
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