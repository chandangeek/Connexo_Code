package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.obis.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies an {@link com.energyict.mdc.protocol.api.device.BaseRegister} based on the ObisCode
 * of the RegisterType or the
 * {@link com.energyict.mdc.device.config.RegisterSpec#getDeviceObisCode() RegisterSpec.getDeviceObisCode}
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/10/12
 * Time: 16:29
 */
public class RegisterDataIdentifier implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final ObisCode deviceRegisterObisCode;
    private final DeviceIdentifier<Device> deviceIdentifier;

    private Register register;

    public RegisterDataIdentifier(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier<Device> deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public Register findRegister() {
        if (this.register == null) {
            List<Register> registers = this.deviceIdentifier.findDevice().getRegisters();
            for (Register register : registers) {
                // first need to check the DeviceObisCode
                if (register.getDeviceObisCode() != null && register.getDeviceObisCode().equals(deviceRegisterObisCode)) {
                    this.register = register;
                    break;
                }
                else if (register.getRegisterTypeObisCode().equals(registerObisCode)) {
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
        return "register having OBIS code " + this.registerObisCode;
    }

    @Override
    public DeviceIdentifier<Device> getDeviceIdentifier() {
        return deviceIdentifier;
    }
}