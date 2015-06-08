package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierType;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterFactory;
import com.energyict.mdw.core.RegisterFactoryProvider;
import com.energyict.obis.ObisCode;
import com.energyict.util.Collections;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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

    private Register register;

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

    @Override
    public Register findRegister() {
        if (this.register == null) {
            Register register = getRegisterFactory().find(id);
            if (register == null) {
                throw new NotFoundException("Register with id " + this.id + " not found");
            }
            this.register = register;
        }
        return this.register;
    }

    @XmlAttribute
    public int getId() {
        return id;
    }

    @XmlAttribute
    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Check if the given {@link Object} is equal to this {@link RegisterIdentifierById}. <BR>
     * WARNING: if comparing with an {@link RegisterIdentifier} of another type (not of type {@link RegisterIdentifierById}),
     * this check will always return false, regardless of the fact they can both point to the same {@link Register}!
     */
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
    public RegisterIdentifierType getRegisterIdentifierType() {
        return RegisterIdentifierType.DataBaseId;
    }

    @Override
    public List<Object> getIdentifier() {
        return Collections.toList((Object) getId());
    }

    private RegisterFactory getRegisterFactory() {
        return RegisterFactoryProvider.instance.get().getRegisterFactory();
    }
}