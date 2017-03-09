package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.obis.ObisCode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(register.getDevice());
    }

    @Override
    public ObisCode getRegisterObisCode() {
        return register.getDeviceObisCode();
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Collections.singletonList("actual"));
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