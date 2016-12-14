package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

import java.text.MessageFormat;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies
 * a {@link com.energyict.mdc.upl.meterdata.Register} based on the ObisCode of the mapping or the
 * ObisCode of the register spec.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:24
 */
public class RegisterDataIdentifierByObisCodeAndDevice implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final DeviceIdentifier deviceIdentifier;
    private final ObisCode deviceRegisterObisCode;

    private Register register;

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister () {
        if (this.register == null) {
            Device device = deviceIdentifier.findDevice();
            for (Register register : device.getRegisters()) {
                // first need to check the DeviceObisCde
                if (register.getDeviceObisCode() != null && register.getDeviceObisCode().equals(registerObisCode)){
                    this.register = register;
                    break;
                } else if(register.getRegisterTypeObisCode().equals(registerObisCode)){
                    this.register = register;
                    break;
                }
            }
        }
        return this.register;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public ObisCode getObisCode() {
        return this.registerObisCode;
    }

    @Override
    public ObisCode getDeviceRegisterObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public String toString() {
        return MessageFormat.format("register having OBIS code {0} on device with deviceIdentifier ''{1}''", registerObisCode, deviceIdentifier);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DeviceIdentifierAndObisCode";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "device": {
                    return deviceIdentifier;
                }
                case "obisCode": {
                    return deviceRegisterObisCode;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}