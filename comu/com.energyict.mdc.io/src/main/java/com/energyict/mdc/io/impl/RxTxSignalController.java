/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.SignalController;

import gnu.io.SerialPort;

public class RxTxSignalController implements SignalController {

    private final SerialPort serialPort;

    public RxTxSignalController(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public boolean signalStateDSR() {
        return this.serialPort.isDSR();
    }

    @Override
    public boolean signalStateCTS() {
        return this.serialPort.isCTS();
    }

    @Override
    public boolean signalStateCD() {
        return this.serialPort.isCD();
    }

    @Override
    public boolean signalStateRing() {
        return this.serialPort.isRI();
    }

    @Override
    public void setDTR(boolean dtr) {
        this.serialPort.setDTR(dtr);
    }

    @Override
    public void setRTS(boolean rts) {
        this.serialPort.setRTS(rts);
    }

}