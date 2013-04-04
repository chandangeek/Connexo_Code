package com.energyict.dlms;

/*
 * DLMSConnection.java
 *
 * Created on 11 oktober 2007, 10:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dlms.aso.ApplicationServiceObject;

import java.io.IOException;

/**
 * @author kvds
 */
public interface DLMSConnection {

    /**
     * Method to read out a response frame, taking into account timeout and retry mechanism. <br></br>
     * No data is send to the device, instead the connection starts immediately reading. If a valid frame could be read before
     * a timeout occurs, the frame is returned. If not the case, a retry request is sent out (according to regular retry mechanism).
     *
     * @param retryRequest The retry request to be sent after a timeout occurs
     * @return the response bytes
     * @throws IOException
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
     * @throws IOException
     */
    byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException;

    byte[] sendRequest(byte[] request) throws IOException;

    byte[] sendRequest(byte[] request, boolean isAlreadyEncrypted) throws IOException;

    void setTimeout(int timeout);

    int getTimeout();

    void sendUnconfirmedRequest(final byte[] request) throws IOException;

    void setHHUSignOn(HHUSignOn hhuSignOn, String meterId);

    HHUSignOn getHhuSignOn();

    void connectMAC() throws IOException, DLMSConnectionException;

    void disconnectMAC() throws IOException, DLMSConnectionException;

    int getType();

    byte[] sendRawBytes(byte[] data) throws IOException;

    void setSNRMType(int type);

    void setIskraWrapper(int type);

    void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler);

    InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler();

    int getMaxRetries();

    ApplicationServiceObject getApplicationServiceObject();

}
