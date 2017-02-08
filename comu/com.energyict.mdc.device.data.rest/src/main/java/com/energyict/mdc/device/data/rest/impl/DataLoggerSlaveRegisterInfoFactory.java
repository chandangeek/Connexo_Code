/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

public class DataLoggerSlaveRegisterInfoFactory {

    DataLoggerSlaveRegisterInfo from(RegisterInfo dataLoggerRegister, Optional<RegisterInfo> slaveRegister){
        DataLoggerSlaveRegisterInfo info = new DataLoggerSlaveRegisterInfo();
        info.dataLoggerRegister = dataLoggerRegister;
        if (slaveRegister.isPresent()){
            info.slaveRegister = slaveRegister.get();
        }
        return info;
    }

}
