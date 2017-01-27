package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s Call Home ID to uniquely identify it.
 *
 * @author khe
 * @since 25/09/14 (10:00)
 */
@XmlRootElement
public class DialHomeIdPlaceHolderDeviceIdentifier implements FindMultipleDevices {

    private final CallHomeIdPlaceHolder callHomeIdPlaceHolder;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DialHomeIdPlaceHolderDeviceIdentifier() {
        callHomeIdPlaceHolder = new CallHomeIdPlaceHolder();
    }

    public DialHomeIdPlaceHolderDeviceIdentifier(CallHomeIdPlaceHolder callHomeIdPlaceHolder) {
        super();
        this.callHomeIdPlaceHolder = callHomeIdPlaceHolder;
    }

    @Override
    public String toString() {
        return "device with call home id " + this.callHomeIdPlaceHolder.getSerialNumber();
    }

    @XmlAttribute
    public CallHomeIdPlaceHolder getCallHomeIdPlaceHolder() {
        return callHomeIdPlaceHolder;
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
                return callHomeIdPlaceHolder.getSerialNumber();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}