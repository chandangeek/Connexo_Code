package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;

/**
 * Represents the simple slave register info.
 */
public class SlaveRegisterInfo {
    public String deviceName;
    public Long registerId;

    public static SlaveRegisterInfo from(Device dataLoggerSlave, Register<?, ?> register) {
        SlaveRegisterInfo slaveRegisterInfo = new SlaveRegisterInfo();
        slaveRegisterInfo.deviceName = dataLoggerSlave.getName();
        slaveRegisterInfo.registerId = register.getRegisterSpecId();
        return slaveRegisterInfo;
    }
}
