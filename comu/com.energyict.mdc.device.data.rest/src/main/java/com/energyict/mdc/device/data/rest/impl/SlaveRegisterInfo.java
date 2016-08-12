package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;

/**
 * Represents the simple slave register info.
 */
public class SlaveRegisterInfo {
    public String mrid;
    public Long registerId;

    public static SlaveRegisterInfo from(Device dataLoggerSlave, Register<?, ?> register) {
        SlaveRegisterInfo slaveRegisterInfo = new SlaveRegisterInfo();
        slaveRegisterInfo.mrid = dataLoggerSlave.getmRID();
        slaveRegisterInfo.registerId = register.getRegisterSpecId();
        return slaveRegisterInfo;
    }
}
