/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2.connection;

import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.InvokeIdAndPriorityHandler;

import java.io.IOException;

/**
 * The interface provides all functionality to start, use and stop a communication session.
 * It can be used by an application layer to handle communication with a device.
 *
 * @author khe
 */
public interface DlmsConnection {

    /**
     * Set up the connection, after this the protocol can fully communicate with the device
     * This connect can consist of e.g. a HDLC signon, an IEC1107 signon (baud rate switching), ...
     *
     * @throws IOException             in case of timeout or other communication error
     * @throws DLMSConnectionException meter didn't accept the request, sign on failed
     */
    void connectMAC() throws IOException, DLMSConnectionException;

    /**
     * Let the device know that we want to end the communication.
     *
     * @throws IOException             in case of timeout or other communication error
     * @throws DLMSConnectionException meter didn't accept the request, sign off failed
     */
    void disconnectMAC() throws IOException, DLMSConnectionException;

    /**
     * Send out a given request (byte array) to the device, read and return the response
     */
    byte[] sendRequest(byte[] request) throws IOException;

    /**
     * Send a request, don't wait for a response
     */
    void sendUnconfirmedRequest(final byte[] request) throws IOException;

    /**
     * Send out a given request, but don't apply any encryption. Read and return the response
     * Use this method for requests that were somehow already encrypted.
     */
    byte[] sendRequest(byte[] request, boolean isAlreadyEncrypted) throws IOException;

    /**
     * Method to read out a response frame, taking into account timeout and retry mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading. If a valid frame could be read before
     * a timeout occurs, the frame is returned. If not the case, a retry request is sent out (according to regular retry mechanism).
     *
     * @param retryRequest The retry request to be sent after a timeout occurs
     * @return the response bytes
     * @throws java.io.IOException
     */
    byte[] readResponseWithRetries(byte[] retryRequest) throws IOException;

    /**
     * Method to read out a response frame, taking into account timeout and retry mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading. If a valid frame could be read before
     * a timeout occurs, the frame is returned. If not the case, a retry request is sent out (according to regular retry mechanism).
     *
     * @param retryRequest       The retry request to be sent after a timeout occurs
     * @param isAlreadyEncrypted Boolean indicating the request is already encrypted
     * @return the response bytes
     * @throws java.io.IOException
     */
    byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException;

    /**
     * Set the HHUSignOn object, it is used to execute the baud rate switching procedure at the start of certain communication sessions.
     * This uses IEC1107 sign on.
     */
    void setHHUSignOn(HHUSignOn hhuSignOn, String meterId);

    /**
     * Set the HHUSignOn object, it is used to execute the baud rate switching procedure at the start of certain communication sessions.
     * This uses IEC1107 sign on.
     *
     * @param hhuSignonBaudRateCode: this is the initial baud rate used to send the identification request.
     *                               After this request, a new baud rate will be negotiated for further usage in the communication session.
     *                               The integer selects on of these baud rates: {300, 600, 1200, 2400, 4800, 9600, 19200}
     */
    void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode);

    /**
     * Returns the HHUSignOn object that executes the baud rate switching procedure at the start of certain communication sessions.
     */
    HHUSignOn getHhuSignOn();

    /**
     * Sends the raw data as it is, without wrapping them in any headers or trailers. Read and return the response
     */
    byte[] sendRawBytes(byte[] data) throws IOException, DLMSConnectionException;

    /**
     * Set the InvokeIdAndPriorityHandler that will handle the InvokeId and priority byte in the DLMS requests and responses.
     * The handler can check and verify the ID (and can reject a response if it has an invalid ID).
     */
    void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler);

    /**
     * Getter for the InvokeIdAndPriorityHandler that will handle the InvokeId and priority byte in the DLMS requests and responses.
     * The handler can check and verify the ID (and can reject a response if it has an invalid ID).
     */
    InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler();

    /**
     * Set the timeout interval of this communication session.
     * Some special requests may take a lot longer than normal requests, this method can be used to set another timeout interval.
     */
    void setTimeout(long timeout);

    /**
     * Getter for the current value of the timeout interval of this communication session.
     * It can be different from the value specified in the properties.
     */
    long getTimeout();

    /**
     * Set the number of retries of this communication session
     */
    void setRetries(int retries);

    /**
     * Getter for the current number of retries of this communication session.
     * It can be different from the value specified in the properties.
     */
    int getMaxRetries();

    /**
     * Getter for the maximum number of tries of this communication session
     */
    int getMaxTries();

}
