package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.obis.ObisCode;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdc.upl.meterdata.Register} based on the ObisCode
 * of the RegisterType or the
 * {@link com.energyict.mdc.device.config.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 * <p>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 16:29
 */
public class RegisterDataIdentifier implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final ObisCode deviceRegisterObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private Register register;

    public RegisterDataIdentifier(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public ObisCode getRegisterObisCode() {
        return this.registerObisCode;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return "register having OBIS code " + this.registerObisCode;
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
                    return registerObisCode;
                }
                case "deviceObisCode": {
                    return deviceRegisterObisCode;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}