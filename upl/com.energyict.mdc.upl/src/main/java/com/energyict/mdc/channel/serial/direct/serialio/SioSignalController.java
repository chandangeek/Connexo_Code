package com.energyict.mdc.channel.serial.direct.serialio;

import Serialio.SerialPort;
import com.energyict.mdc.channel.serial.SignalController;
import com.energyict.mdc.upl.io.SerialPortException;

import java.io.IOException;

/**
 * The SignalController for a SerialPort working with the SerialIO library
 * <p>
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 16:19
 */
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
            throw SerialPortException.serialLibraryException(e);
        }
    }

    @Override
    public boolean signalStateCTS() {
        try {
            return this.serialPort.sigCTS();
        } catch (IOException e) {
            throw SerialPortException.serialLibraryException(e);
        }
    }

    @Override
    public boolean signalStateCD() {
        try {
            return this.serialPort.sigCD();
        } catch (IOException e) {
            throw SerialPortException.serialLibraryException(e);
        }
    }

    @Override
    public boolean signalStateRing() {
        try {
            return this.serialPort.sigRing();
        } catch (IOException e) {
            throw SerialPortException.serialLibraryException(e);
        }
    }

    @Override
    public void setDTR(boolean dtr) {
        try {
            this.serialPort.setDTR(dtr);
        } catch (IOException e) {
            throw SerialPortException.serialLibraryException(e);
        }
    }

    @Override
    public void setRTS(boolean rts) {
        try {
            this.serialPort.setRTS(rts);
        } catch (IOException e) {
            throw SerialPortException.serialLibraryException(e);
        }
    }
}