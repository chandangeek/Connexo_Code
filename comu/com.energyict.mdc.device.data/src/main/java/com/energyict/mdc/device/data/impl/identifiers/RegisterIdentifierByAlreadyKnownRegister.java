package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifierType;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/12/2016 - 16:04
 */
public class RegisterIdentifierByAlreadyKnownRegister implements RegisterIdentifier {

    private final Register register;

    public RegisterIdentifierByAlreadyKnownRegister(com.energyict.mdc.upl.meterdata.Register register) {
        this.register = (Register) register;    //Downcast to the Connexo Register
    }

    @Override
    public Register findRegister() {
        return register;
    }

    @Override
    public ObisCode getRegisterObisCode() {
        return register.getDeviceObisCode();
    }

    @Override
    public RegisterIdentifierType getRegisterIdentifierType() {
        return RegisterIdentifierType.ActualRegister;
    }

    @Override
    public List<Object> getParts() {
        return Collections.singletonList(register);
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(register.getDevice());
    }
}