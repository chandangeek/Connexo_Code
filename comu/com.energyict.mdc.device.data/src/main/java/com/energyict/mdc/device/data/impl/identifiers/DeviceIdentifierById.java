package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses a {@link com.energyict.mdc.upl.meterdata.Device}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierById implements DeviceIdentifier {

    private long id;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierById() {
    }

    public DeviceIdentifierById(long id) {
        this();
        this.id = id;
    }

    // used for reflection
    public DeviceIdentifierById(String id) {
        super();
        this.id = Long.parseLong(id);
    }

    @Override
    public String toString() {
        return "device having id " + this.id;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceIdentifierById that = (DeviceIdentifierById) other;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("databaseValue"));
        }


        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return id;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}