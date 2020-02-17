package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link Device}'s Call Home ID to uniquely identify it.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
@XmlRootElement
public class DialHomeIdDeviceIdentifier implements FindMultipleDevices {

    private final String callHomeID;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DialHomeIdDeviceIdentifier() {
        callHomeID = "";
    }

    public DialHomeIdDeviceIdentifier(String callHomeId) {
        super();
        this.callHomeID = callHomeId;
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeID;
    }

    @XmlAttribute
    public String getCallHomeID() {
        return callHomeID;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "CallHomeId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("callHomeId"));
        }

        @Override
        public Object getValue(String role) {
            if ("callHomeId".equals(role)) {
                return callHomeID;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

}