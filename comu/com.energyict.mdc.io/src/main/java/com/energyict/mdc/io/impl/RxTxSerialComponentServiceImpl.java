package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ModemComponent;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SerialPortConfiguration;
import com.energyict.mdc.io.ServerSerialPort;

import com.elster.jupiter.time.TimeDuration;
import org.osgi.service.component.annotations.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides an implementation for the {@link SerialComponentService} interface
 * that uses the RxTx library.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-03 (09:15)
 */
public abstract class RxTxSerialComponentServiceImpl extends AbstractSerialComponentServiceImpl {

    @Override
    public ServerSerialPort newSerialPort(SerialPortConfiguration configuration) {
        return new RxTxSerialPort(configuration);
    }

}