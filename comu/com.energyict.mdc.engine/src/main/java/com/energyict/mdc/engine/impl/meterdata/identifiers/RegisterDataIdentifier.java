package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdc.protocol.api.device.BaseRegister} based on the ObisCode
 * of the RegisterMapping or the
 * {@link com.energyict.mdc.device.config.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 16:29
 */
public class RegisterDataIdentifier implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final ObisCode deviceRegisterObisCode;
    private final DeviceIdentifier deviceIdentifier;

    private BaseRegister register;

    public RegisterDataIdentifier(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public BaseRegister findRegister() {
        if (this.register == null) {
            DeviceIdentifier deviceFinder = deviceIdentifier;

            final List<BaseRegister> registers = deviceFinder.findDevice().getRegisters();
            for (BaseRegister register : registers) {
                // first need to check the DeviceObisCde
                if (register.getDeviceObisCode() != null && register.getDeviceObisCode().equals(registerObisCode)) {
                    this.register = register;
                    break;
                }
                else if (register.getRegisterMappingObisCode().equals(registerObisCode)) {
                    this.register = register;
                    break;
                }
            }
        }
        return this.register;
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
        return this.registerObisCode.toString();
    }
}
