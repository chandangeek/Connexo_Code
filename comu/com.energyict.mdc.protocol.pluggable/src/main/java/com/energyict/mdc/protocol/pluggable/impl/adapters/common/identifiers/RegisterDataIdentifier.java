package com.energyict.mdc.protocol.pluggable.impl.adapters.common.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.RegisterFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-15 (16:17)
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
    public BaseRegister findRegister () {
        if(this.register == null){
            DeviceIdentifier deviceFinder = deviceIdentifier;
            List<RegisterFactory> registerFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(RegisterFactory.class);
            BaseDevice device = deviceFinder.findDevice();
            for (RegisterFactory registerFactory : registerFactories) {
                List<BaseRegister> registers = registerFactory.findRegistersByDevice(device);
                for (BaseRegister register : registers) {
                    if (register.getDeviceObisCode() != null && register.getDeviceObisCode().equals(registerObisCode)){
                        this.register = register;
                        break;
                    } else if(register.getRegisterMappingObisCode().equals(registerObisCode)){
                        this.register = register;
                        break;
                    }
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