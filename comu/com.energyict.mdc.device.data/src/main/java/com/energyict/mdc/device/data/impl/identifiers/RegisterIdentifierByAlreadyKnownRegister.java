package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

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
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(register.getDevice());
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return register;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}