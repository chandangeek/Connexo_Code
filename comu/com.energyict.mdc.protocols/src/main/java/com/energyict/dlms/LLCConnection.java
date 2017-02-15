/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jme
 */
public class LLCConnection extends CosemPDUConnection {

    private static final Logger logger = Logger.getLogger(LLCConnection.class.getName());

    public LLCConnection(InputStream inputStream, OutputStream outputStream, int timeout, int forceDelay, int maxRetries, int clientAddress, int serverAddress)
            throws IOException {
        super(inputStream, outputStream, timeout, forceDelay, maxRetries, clientAddress, serverAddress);
    }

    private byte[] receiveData() throws IOException {
        byte[] data;
        long interFrameTimeout;
        copyEchoBuffer();
        DLMSUtils.delay(getForceDelay());
        interFrameTimeout = System.currentTimeMillis() + getTimeout();
        while (true) {
            data = readInArray();
            if (data != null) {
                if (data[0] == (byte) 0x90) {
                    return data;
                } else {
                    while (readInArray() != null) {
                    }
                    throw new ProtocolException("LLC packet should start with 0x90!");
                }
            }
            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ConnectionException("receiveData() response timeout error", TIMEOUT_ERROR);
            }
        }
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        boolean firstRead = true;
        // this.currentTryCount contains the current try number - we should not start again from 0, but continue from current try number

        byte[] byteRequestBuffer = new byte[retryRequest.length];
        System.arraycopy(retryRequest, 3, byteRequestBuffer, 3, retryRequest.length - 3);
        byteRequestBuffer[0] = (byte) 0x90;
        byteRequestBuffer[1] = (byte) 0x01;
        byteRequestBuffer[2] = (byte) 0x02;

        while (true) {
            try {
                if (firstRead) {
                    firstRead = false;      // In the first iteration, do not send a retry, but start directly reading
                } else {
                    sendOut(retryRequest);  // Do send out retry request
                    DLMSUtils.delay(getForceDelay());
                }
                return receiveData();
            } catch (ConnectionException e) {
                this.logger.warning(e.getMessage());
                if (this.currentTryCount++ >= getMaxRetries()) {
                    throw new NestedIOException(e, "readResponseWithRetries, IOException");
                }
            }
        }
    }

    @Override
    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        return this.readResponseWithRetries(retryRequest);
    }

    /**
     * Append the LLC header to the packet (Hard coded to 0x90, 0x01, 0x02)
     */
    public byte[] sendRequest(byte[] data) throws IOException {

        resetCurrentTryCount();

        byte[] byteRequestBuffer = new byte[data.length];
        System.arraycopy(data, 3, byteRequestBuffer, 3, data.length - 3);
        byteRequestBuffer[0] = (byte) 0x90;
        byteRequestBuffer[1] = (byte) 0x01;
        byteRequestBuffer[2] = (byte) 0x02;

        while (true) {
            try {
                sendOut(byteRequestBuffer);
                DLMSUtils.delay(getForceDelay());
                return receiveData();
            } catch (ConnectionException e) {
                if (this.currentTryCount++ >= getMaxRetries()) {
                    throw new IOException("sendRequest, IOException", e);
                }
            }
        }
    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    @Override
    public void sendUnconfirmedRequest(final byte[] request) throws IOException {
        resetCurrentTryCount();

        byte[] byteRequestBuffer = new byte[request.length];
        System.arraycopy(request, 3, byteRequestBuffer, 3, request.length - 3);
        byteRequestBuffer[0] = (byte) 0x90;
        byteRequestBuffer[1] = (byte) 0x01;
        byteRequestBuffer[2] = (byte) 0x02;

        while (true) {
            try {
                sendOut(byteRequestBuffer);
                DLMSUtils.delay(getForceDelay());
                return;
            } catch (ConnectionException e) {
                if (this.currentTryCount++ >= getMaxRetries()) {
                    throw new IOException("sendRequest, IOException", e);
                }

                logger.log(Level.WARNING, "Sleeping for [" + getTimeout() + " ms] until next try ...");
                DLMSUtils.delay(getTimeout());

            }
        }
    }
}
