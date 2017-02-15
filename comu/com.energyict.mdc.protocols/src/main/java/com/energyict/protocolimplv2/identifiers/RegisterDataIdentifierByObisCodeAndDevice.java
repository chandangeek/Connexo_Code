/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.text.MessageFormat;

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
        return MessageFormat.format("register having OBIS code {0} on device with deviceIdentifier ''{1}''", registerObisCode, deviceIdentifier);
    }

    public DeviceIdentifier<?> getDeviceIdentifier() {
        return deviceIdentifier;
    }
}
