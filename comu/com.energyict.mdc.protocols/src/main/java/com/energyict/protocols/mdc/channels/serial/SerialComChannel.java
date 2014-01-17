package com.energyict.protocols.mdc.channels.serial;

import com.energyict.protocols.mdc.channels.SynchroneousComChannel;
import com.energyict.mdc.protocol.api.ComChannel;

/**
 * Creates a simple {@link ComChannel} wrapped around a {@link ServerSerialPort}
 * <p/>
 * Copyrights EnergyICT
 * Date: 17/08/12
 * Time: 9:34
 */
public class SerialComChannel extends SynchroneousComChannel {

    private final ServerSerialPort serialPort;

    public SerialComChannel(ServerSerialPort serialPort) {
        super(serialPort.getInputStream(), serialPort.getOutputStream());
        this.serialPort = serialPort;
    }

    @Override
    public void doClose() {
        try {
            super.doClose();
        } finally {
            if (this.serialPort != null) {
                this.serialPort.close();
            }
        }
    }

    public ServerSerialPort getSerialPort(){
        return this.serialPort;
    }
}
