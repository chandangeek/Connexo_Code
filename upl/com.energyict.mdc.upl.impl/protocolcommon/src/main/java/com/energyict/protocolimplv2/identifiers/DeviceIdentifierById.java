package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses the Device's database identifier.
 * Copyrights EnergyICT
 *
 * @author Kristof Hennebel (khe)
 * @since 2013-06-07 (09:31)
 */
@XmlRootElement
public class DeviceIdentifierById implements DeviceIdentifier {

    private int id;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private DeviceIdentifierById() {
    }

    public DeviceIdentifierById(int id) {
        super();
        this.id = id;
    }

    public DeviceIdentifierById(long id) {
        this((int) id);
    }

    // used for reflection
    public DeviceIdentifierById(String id) {
        super();
        this.id = Integer.parseInt(id);
    }

    @Override
    public String toString () {
        return "id " + this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeviceIdentifierById)) {
            return false;
        }
        DeviceIdentifierById identifier = (DeviceIdentifierById) obj;
        return identifier.getId() == this.getId();
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
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