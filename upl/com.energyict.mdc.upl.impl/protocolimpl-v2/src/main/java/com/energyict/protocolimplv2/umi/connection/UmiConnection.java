package com.energyict.protocolimplv2.umi.connection;

import com.energyict.mdc.protocol.ComChannel;

import java.io.IOException;

public interface UmiConnection {
    /**
     * Send out a given byte array to the device
     */
    void send(byte[] data) throws IOException;

    /**
     * Method to read out a response data, taking into account timeout mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading.
     * If a valid data could be read before a timeout occurs, the data is returned.
     *
     * @return the response bytes
     * @throws java.io.IOException
     */
    byte[] receive() throws IOException;

    /**
     * Set the timeout interval of this communication session.
     * Some special requests may take a lot longer than normal requests,
     * this method can be used to set another timeout interval.
     */
    void setTimeout(long timeout);

    /**
     * Getter for the current value of the timeout interval of this communication session.
     * It can be different from the value specified in the properties.
     */
    long getTimeout();

    /**
     * Getter for ComChannel object.
     */
    ComChannel getComChannel();
}