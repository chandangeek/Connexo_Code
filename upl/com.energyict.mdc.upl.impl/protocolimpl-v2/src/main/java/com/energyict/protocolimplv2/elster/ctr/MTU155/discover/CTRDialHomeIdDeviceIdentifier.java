package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface,
 * specific for the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * These protocols use a Call Home ID to uniquely identify the calling device.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
@XmlRootElement
public class CTRDialHomeIdDeviceIdentifier implements FindMultipleDevices {

    private final String callHomeID;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private CTRDialHomeIdDeviceIdentifier() {
        callHomeID = "";
    }

    public CTRDialHomeIdDeviceIdentifier(String callHomeId) {
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

}