package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import com.energyict.obis.ObisCode;

/**
 * Copyrights EnergyICT
 * Date: 2/23/15
 * Time: 3:16 PM
 */
public class DeviceIdentifierByLogBook implements DeviceIdentifier {

    private final LogBookIdentifier logBookIdentifier;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    @SuppressWarnings("unused")
    public DeviceIdentifierByLogBook() {
        this(new NullLogBookIdentifier());
    }

    public DeviceIdentifierByLogBook(LogBookIdentifier logBookIdentifier) {
        super();
        this.logBookIdentifier = logBookIdentifier;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByLogBook)) {
            return false;
        }

        DeviceIdentifierByLogBook that = (DeviceIdentifierByLogBook) o;

        return logBookIdentifier.equals(that.logBookIdentifier);

    }

    @Override
    public int hashCode() {
        return logBookIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having logbook identified by '" + this.logBookIdentifier + "'";
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Other";
        }

        @Override
        public Object getValue(String role) {
            return logBookIdentifier.forIntrospection().getValue(role);
        }
    }

    private static class NullLogBookIdentifier implements LogBookIdentifier {
        @Override
        public ObisCode getLogBookObisCode() {
            return null;
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

}