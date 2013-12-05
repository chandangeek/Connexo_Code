package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.meterdata.identifiers.CanFindRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdw.amr.Register} based on the ObisCode
 * of the {@link com.energyict.mdw.amr.RegisterMapping RegisterMapping} or the
 * {@link com.energyict.mdw.amr.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 *
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:24
 */
public class RegisterDataIdentifierByObisCodeAndDevice implements CanFindRegister {

    private final ObisCode registerObisCode;
    private final CanFindDevice deviceIdentifier;

    private Register register;

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, CanFindDevice deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister () {
        if(this.register == null){
            final List<Register> registers = MeteringWarehouse.getCurrent().getRegisterFactory().findByRtu(deviceIdentifier.findDevice());
            for (Register register : registers) {
                // first need to check the DeviceObisCde
                if (register.getRegisterSpec().getDeviceObisCode() != null && register.getRegisterSpec().getDeviceObisCode().equals(registerObisCode)){
                    this.register = register;
                    break;
                } else if(register.getRegisterMapping().getObisCode().equals(registerObisCode)){
                    this.register = register;
                    break;
                }
            }
        }
        return this.register;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + this.deviceIdentifier + " and ObisCode = " + this.registerObisCode.toString();
    }

    public ObisCode getRegisterObisCode() {
        return registerObisCode;
    }

    public CanFindDevice getDeviceIdentifier() {
        return deviceIdentifier;
    }
}
