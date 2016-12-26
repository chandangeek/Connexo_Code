package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 29/11/13
 * Time: 9:51
 * Author: khe
 */
@XmlRootElement
public class RegisterIdentifierById implements RegisterIdentifier {

    private final int id;
    private final ObisCode registerObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private RegisterIdentifierById() {
        this.id = 0;
        this.registerObisCode = null;
    }

    public RegisterIdentifierById(int id, ObisCode registerObisCode) {
        this.id = id;
        this.registerObisCode = registerObisCode;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterIdentifierById otherIdentifier = (RegisterIdentifierById) o;
        return (this.id == otherIdentifier.id);
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
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
                return getId();
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}