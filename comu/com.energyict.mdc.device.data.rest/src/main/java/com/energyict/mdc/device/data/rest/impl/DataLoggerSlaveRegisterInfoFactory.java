package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

/**
 * Creating DataLoggerSlaveRegisterInfo given a slave register and a data logger register
 * Copyrights EnergyICT
 * Date: 10/05/2016
 * Time: 11:14
 */
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
