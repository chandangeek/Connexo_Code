package com.energyict.dlms.protocolimplv2.connection;

/**
 * All V2 DLMS Connections should implement this interface.
 * This has the same methods as {@link DlmsConnection} except for:
 * There's no exception in the signatures, because all implementations throw the proper ComServer runtime exception when some error occurs.
 * <p/>
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 14:19
 * Author: khe
 */
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
     */
    byte[] sendRequest(byte[] request);

    /**
     * Send a request, don't wait for a response
     */
    void sendUnconfirmedRequest(final byte[] request);

    /**
     * Send out a given request, but don't apply any encryption. Read and return the response
     * Use this method for requests that were somehow already encrypted.
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
     * Sends the raw data as it is, without wrapping them in any headers or trailers. Read and return the response
     */
    byte[] sendRawBytes(byte[] data);

}
