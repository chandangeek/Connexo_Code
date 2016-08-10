package com.energyict.mdc.device.data.rest.impl;

import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Copyrights EnergyICT
 * Date: 22/06/2016
 * Time: 15:14
 */
public class DataLoggerSlaveRegisterInfoFactoryTest {

    @Test
    public void fromTestNoSlave(){
        NumericalRegisterInfo dataLoggerRegister = new NumericalRegisterInfo();

        DataLoggerSlaveRegisterInfo info = new DataLoggerSlaveRegisterInfoFactory().from(dataLoggerRegister, Optional.empty());

        assertThat(info.dataLoggerRegister).isEqualTo(dataLoggerRegister);
        assertThat(info.slaveRegister).isNull();
        assertThat(info.availabilityDate).isNull();
    }

    @Test
    public void fromTestWithSlave(){
        NumericalRegisterInfo dataLoggerRegister = new NumericalRegisterInfo();
        NumericalRegisterInfo slaveRegister = new NumericalRegisterInfo();

        DataLoggerSlaveRegisterInfo info = new DataLoggerSlaveRegisterInfoFactory().from(dataLoggerRegister, Optional.of(slaveRegister));

        assertThat(info.dataLoggerRegister).isEqualTo(dataLoggerRegister);
        assertThat(info.slaveRegister).isEqualTo(slaveRegister);
        assertThat(info.availabilityDate).isNull();
    }



}
