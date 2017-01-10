package com.energyict.mdc.channels.serial.direct.rxtx;

import com.energyict.mdc.channels.serial.SignalController;

import gnu.io.SerialPort;

/**
 * The SignalController for a SerialPort working with the RxTx library.
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 16:23
 */
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
