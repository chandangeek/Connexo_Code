/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;

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
