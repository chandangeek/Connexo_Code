package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the serialio library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:10)
 */
public abstract class SerialIOComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new SioSerialPort(configuration);
    }

}