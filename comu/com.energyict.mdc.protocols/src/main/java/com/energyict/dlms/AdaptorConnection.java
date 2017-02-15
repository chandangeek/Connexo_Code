/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AdaptorConnection implements DLMSConnection {

    ByteArrayOutputStream baos;
    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;

    public AdaptorConnection() {
        invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
    }

    public byte[] readResponseWithRetries(byte[] retryRequest) throws IOException {
        throw new IOException("AdaptorConnection, readResponseWithRetries method not supported");
    }

    public byte[] readResponseWithRetries(byte[] retryRequest, boolean isAlreadyEncrypted) throws IOException {
        throw new IOException("AdaptorConnection, readResponseWithRetries method not supported");
    }

    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        baos.write(byteRequestBuffer, 3, byteRequestBuffer.length - 3); // skip HDLC LLC
        return null;
    }

    public byte[] sendRequest(final byte[] encryptedRequest, boolean isAlreadyEncrypted) throws IOException {
        return sendRequest(encryptedRequest);
    }

    public void setTimeout(long timeout) {
    }

    public long getTimeout() {
        return 0;
    }

    public void setRetries(int retries) {
    }

    public void sendUnconfirmedRequest(final byte[] byteRequestBuffer) throws IOException {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        baos.write(byteRequestBuffer, 3, byteRequestBuffer.length - 3); // skip HDLC LLC
    }

    public void reset() {
        baos.reset();
    }

    public byte[] getCompoundData() {
        return baos.toByteArray();
    }

    public void connectMAC() throws IOException, DLMSConnectionException {
        // Nothing to do
    }

    public void disconnectMAC() throws IOException, DLMSConnectionException {
        // Nothing to do
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public byte[] sendRawBytes(byte[] data) throws IOException {
        return new byte[0];
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId) {
        // Nothing to do
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn, String meterId, int hhuSignonBaudRateCode) {
        // Nothing to do
    }

    public void setIskraWrapper(int type) {
        // Nothing to do
    }

    public void setSNRMType(int type) {
        // Nothing to do
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return this.invokeIdAndPriorityHandler;
    }

    public void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler iiapHandler) {
        this.invokeIdAndPriorityHandler = iiapHandler;
    }

    public int getMaxRetries() {
        return 0;
    }

    @Override
    public int getMaxTries() {
        return 0;
    }
}