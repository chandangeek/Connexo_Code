/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SerialPortException;
import com.energyict.mdc.io.SignalController;

import Serialio.SerialPort;

import java.io.IOException;

public class SioSignalController implements SignalController {

    private final SerialPort serialPort;

    public SioSignalController(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public boolean signalStateDSR() {
        try {
            return this.serialPort.sigDSR();
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    @Override
    public boolean signalStateCTS() {
        try {
            return this.serialPort.sigCTS();
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    @Override
    public boolean signalStateCD() {
        try {
            return this.serialPort.sigCD();
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    @Override
    public boolean signalStateRing() {
        try {
            return this.serialPort.sigRing();
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    @Override
    public void setDTR(boolean dtr) {
        try {
            this.serialPort.setDTR(dtr);
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }

    @Override
    public void setRTS(boolean rts) {
        try {
            this.serialPort.setRTS(rts);
        } catch (IOException e) {
            throw new SerialPortException(MessageSeeds.SERIAL_PORT_LIBRARY_EXCEPTION, e);
        }
    }
}
