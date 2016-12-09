package com.energyict.mdc.channels.serial.modem.postdialcommand;

import com.energyict.mdc.protocol.ComChannel;

/**
 * Copyrights EnergyICT
 * Date: 09/12/16
 * Time: 15:23
 */
public interface ModemComponent {
    void flush(ComChannel comChannel, long milliSecondsOfSilence);

    void delay(long delay);

    void writeRawData(ComChannel comChannel, String command);
}
