package com.energyict.mdc.channel.serial.modemproperties.postdialcommand;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.SerialPortComChannel;

/**
 * Copyrights EnergyICT
 * Date: 09/12/16
 * Time: 15:23
 */
public interface ModemComponent {

    void flush(ComChannel comChannel, long milliSecondsOfSilence);

    void delay(long delay);

    void connect(String name, SerialPortComChannel comChannel);

    void disconnect(SerialPortComChannel comChannel);

    /**
     * Initialize the modem so it is ready for dialing/receival of a call.
     * During this initialization, several steps are performed:<br>
     * <ul>
     * <li>If present, the current connection of the modem is hung up</li>
     * <li>The default profile of the modem is restored.</li>
     * <li>All initialization strings are send out to the modem</li>
     * </ul>
     */
    void initializeModem(String name, SerialPortComChannel comChannel);

    /**
     * Initialization method to be performed right after the modem of the device has established a connection.
     *
     * @param comChannel The newly created ComChannel
     */
    void initializeAfterConnect(ComChannel comChannel);

    /**
     * Reads bytes from the comChannel and verifies against the given expected value.
     * No retries are performed, just once.
     *
     * @param comChannel      the ComChannel to read
     * @param expectedAnswer  the expected response
     * @param timeOutInMillis the timeOut in milliseconds to wait before throwing a TimeOutException
     * @return true if the answer matches the expected answer, false otherwise
     */
    boolean readAndVerify(ComChannel comChannel, String expectedAnswer, long timeOutInMillis);

    /**
     * Write the given data to the comChannel.
     *
     * @param comChannel  the comChannel to write to
     * @param dataToWrite the data to write
     */
    void write(ComChannel comChannel, String dataToWrite, boolean confirm);

    default void write(ComChannel comChannel, String dataToWrite) {
        this.write(comChannel, dataToWrite, true);
    }
}