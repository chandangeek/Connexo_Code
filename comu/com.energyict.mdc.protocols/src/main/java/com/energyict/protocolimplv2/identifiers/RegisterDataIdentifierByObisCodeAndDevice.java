package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.RegisterFactory;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a {@link RegisterIdentifier} that uniquely identifies
 * a {@link com.energyict.mdc.protocol.api.device.BaseRegister} based on the ObisCode of the mapping or the
 * ObisCode of the register spec.
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:24
 */
public class RegisterDataIdentifierByObisCodeAndDevice implements RegisterIdentifier {

    private final ObisCode registerObisCode;
    private final DeviceIdentifier<?> deviceIdentifier;
    private final ObisCode deviceRegisterObisCode;

    private BaseRegister register;

    public RegisterDataIdentifierByObisCodeAndDevice(ObisCode registerObisCode, ObisCode deviceRegisterObisCode, DeviceIdentifier<?> deviceIdentifier) {
        this.registerObisCode = registerObisCode;
        this.deviceRegisterObisCode = deviceRegisterObisCode;
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public BaseRegister findRegister () {
        if (this.register == null) {
            List<RegisterFactory> registerFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(RegisterFactory.class);
            BaseDevice<? extends BaseChannel, ? extends BaseLoadProfile<? extends BaseChannel>, ? extends BaseRegister> device = deviceIdentifier.findDevice();
            for (BaseRegister register : device.getRegisters()) {
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
    public ObisCode getObisCode() {
        return this.registerObisCode;
    }

    @Override
    public ObisCode getDeviceRegisterObisCode() {
        return this.deviceRegisterObisCode;
    }

    @Override
    public String toString() {
        return "deviceIdentifier = " + this.deviceIdentifier + " and ObisCode = " + this.registerObisCode.toString();
    }

    public DeviceIdentifier<?> getDeviceIdentifier() {
        return deviceIdentifier;
    }
}
