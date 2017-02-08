package com.energyict.dlms;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5/22/12
 * Time: 2:47 PM
 */
public class IF2HHUSignon implements HHUSignOn {

    /**
     * The default baudrate if there is no baudrate given
     */
    private static final int DEFAULT_BAUDRATE = 9600;

    /**
     * The default delay after switching the baudrate to a new value
     */
    private static final int DELAY_AFTER_SWITCH = 500;

    /**
     * The serial communication channel, used to change the connection settings (baudrate, parity, ...)
     */
    private final SerialCommunicationChannel serialCommunicationChannel;

    /**
     * The logger used in this IF2HHUSignon object. This logger is never null
     */
    private final Logger logger;

    /**
     * This HHUSignOn object switches the baudrate of the serial connection.
     * The default baudrate is {@link IF2HHUSignon#DEFAULT_BAUDRATE}
     *
     * @param serialCommunicationChannel The serial communication channel, used to change the connection settings (baudrate, parity, ...)
     * @param logger                     The logger
     */
    public IF2HHUSignon(SerialCommunicationChannel serialCommunicationChannel, Logger logger) {
        if (serialCommunicationChannel == null) {
            throw new IllegalArgumentException("IF2HHUSignon needs a serialCommunicationChannel, but serialCommunicationChannel was 'null'");
        }
        this.serialCommunicationChannel = serialCommunicationChannel;
        this.logger = logger == null ? Logger.getLogger(IF2HHUSignon.class.getName()) : logger;
    }

    /**
     * Switch the baudrate to the default value {@link IF2HHUSignon#DEFAULT_BAUDRATE}
     *
     * @param strIdent Not used
     * @param meterID  Not used
     * @return A dummy MeterType
     * @throws IOException         If there occurred an error while switching the baudrate
     * @throws ConnectionException If there occurred an error while switching the baudrate
     */
    public MeterType signOn(String strIdent, String meterID) throws IOException {
        return signOn(strIdent, meterID, DEFAULT_BAUDRATE);
    }

    /**
     * Switch the baudrate to a given value
     *
     * @param strIdent Not used
     * @param meterID  Not used
     * @param baudrate The baudrate value to switch to (eg. 9600, 115200, 300, ...)
     * @return A dummy MeterType
     * @throws IOException         If there occurred an error while switching the baudrate
     * @throws ConnectionException If there occurred an error while switching the baudrate
     */
    public MeterType signOn(String strIdent, String meterID, int baudrate) throws IOException {
        return signOn(strIdent, meterID, false, baudrate);
    }

    /**
     * Switch the baudrate to a given value
     *
     * @param strIdent Not used
     * @param meterID  Not used
     * @param wakeup   Not used
     * @param baudrate The baudrate value to switch to (eg. 9600, 115200, 300, ...)
     * @return A dummy MeterType
     * @throws IOException         If there occurred an error while switching the baudrate
     * @throws ConnectionException If there occurred an error while switching the baudrate
     */
    public MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate) throws IOException {
        this.logger.info("Switching serial channel to [" + baudrate + "] baud.");
        this.serialCommunicationChannel.setBaudrate(baudrate);
        try {
            Thread.sleep(DELAY_AFTER_SWITCH);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        return new MeterTypeImpl(getReceivedIdent());
    }

    public void sendBreak() {
    }

    /**
     * Not implemented
     *
     * @param protocol Not used
     */
    public void setProtocol(int protocol) {
    }

    /**
     * Not implemented
     *
     * @param mode Not used
     */
    public void setMode(int mode) {
    }

    /**
     * Not implemented
     *
     * @param enabled Not used
     */
    public void enableDataReadout(boolean enabled) {
    }

    /**
     * Not implemented. Returns empty array
     *
     * @return byte[0]
     */
    public byte[] getDataReadout() {
        return new byte[0];
    }

    /**
     * Not implemented. Returns fixed dummy value 'IF2HHUSignon'
     *
     * @return "IF2HHUSignon"
     */
    public String getReceivedIdent() {
        return "IF2HHUSignon";
    }

}
