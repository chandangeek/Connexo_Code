/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.protocolimplv2.connection;

public interface DlmsV2Connection extends DlmsConnection {

    /**
     * Set up the connection, after this the protocol can fully communicate with the device
     * This connect can consist of e.g. a HDLC signon, an IEC1107 signon (baud rate switching), ...
     */
    void connectMAC();

    /**
     * Let the device know that we want to end the communication.
     */
    void disconnectMAC();

    /**
     * Send out a given request (byte array) to the device, read and return the response
     * <p/>
     * The sendRequest will check the current securitySuite to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
     * The response from the meter is decrypted before sending it back to the object.
     */
    byte[] sendRequest(byte[] request);

    /**
     * Send out a given request (byte array) to the device, read and return the response
     * <p/>
     * The sendRequest will check the current securitySuite to encrypt or authenticate the data and then parse the APDU to the DLMSConnection.
     * The response from the meter is decrypted before sending it back to the object.
     * <p/>
     * If the request is already encrypted (indicated by the boolean), there's no need to encrypt it again in the sendRequest() method
     * An example use case would be where the cryptoserver provides us the encrypted APDU to change the P2 MBus key, we only have to send it to the meter.
     *
     * @param request - The unEncrypted/authenticated request
     * @return the unEncrypted response from the device
     */
    byte[] sendRequest(byte[] request, boolean isAlreadyEncrypted);

    /**
     * Method to read out a response frame, taking into account timeout and retry mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading. If a valid frame could be read before
     * a timeout occurs, the frame is returned. If not the case, a retry request is sent out (according to regular retry mechanism).
     *
     * @param retryRequest The retry request to be sent after a timeout occurs
     * @return the response bytes
     */
    byte[] readResponseWithRetries(byte[] retryRequest);

    /**
     * Method to read out a response frame, taking into account timeout and retry mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading. If a valid frame could be read before
     * a timeout occurs, the frame is returned. If not the case, a retry request is sent out (according to regular retry mechanism).
     *
     * @param retryRequest       The retry request to be sent after a timeout occurs
     * @param isAlreadyEncrypted Boolean indicating the request is already encrypted
     * @return the response bytes
     */
    byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted);

    /**
     * Send a request, don't wait for a response
     */
    void sendUnconfirmedRequest(final byte[] request);

    /**
     * Sends the raw data as it is, without wrapping them in any headers or trailers. Read and return the response
     */
    byte[] sendRawBytes(byte[] data);

    /**
     * Getter for boolean indicating whether or not general-blcok-transfer should be used or not
     */
    boolean useGeneralBlockTransfer();

    /**
     * Getter for the preferred general-block-transfer window size<br/>
     * If -1 is returned, then general-block-transfer is disabled
     */
    int getGeneralBlockTransferWindowSize();

    /**
     * Prepare the underlying ComChannel for the receive of a next packet<br/>
     * This method should be executed between receive of multiple packets (e.g. as used in general-block-transfer process)<br/>
     * <br/>
     * During this process the ComChannel will toggle from reading mode to witting mode;
     * the 'sessionCounters' will also be toggled. By doing so, we ensure when the next packet is read,
     * its logging is correctly interpreted (or in other words: the bytes will be logged as a separate 'Rx' entry and the 'Number of packets received' will get incremented)
     */
    void prepareComChannelForReceiveOfNextPacket();

}
