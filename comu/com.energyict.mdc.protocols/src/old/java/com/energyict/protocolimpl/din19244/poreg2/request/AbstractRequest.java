/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

abstract public class AbstractRequest {

    protected Poreg poreg;
    protected boolean corruptFrame = false;
    protected String corruptCause = "";

    //Fields used for register requests only
    protected int receivedRegisterAddress;
    protected int receivedFieldAddress;
    protected int receivedNumberOfRegisters;
    private int previouslyReceivedNumberOfRegisters = 0;
    protected int totalReceivedNumberOfRegisters = 0;
    protected int receivedNumberOfFields;
    protected int registerAddress;
    protected int fieldAddress;
    protected int numberOfRegisters;
    protected int numberOfFields;

    public AbstractRequest(Poreg poreg) {
        this.poreg = poreg;
    }

    protected abstract void parse(byte[] data) throws IOException;

    private void parseWriteResponse(byte[] data) throws IOException {
        if (data.length == 3) {
            if ((data[0] & 0xFF) == Response.ACK.getId()) {
                return;
            }
        }
        throw new IOException("Error writing register, expected ACK, received " + Response.getDescription(data[0] & 0xFF));
    }

    public void doRequest() throws IOException {
        corruptFrame = true;
        int count = 0;
        while (corruptFrame) {
            doTheRequest();      //First attempt

            if (corruptFrame) {
                poreg.getLogger().warning("Received corrupted frame for request (ASDU = " + (getRequestASDU()[0] & 0xFF) + ")");
                poreg.getLogger().warning("Cause: " + corruptCause);

                count++;     //Retry counter
                if (count > poreg.getConnection().getRetries()) {  //Stop retrying after X retries
                    String msg = "Still received a corrupt frame (" + "after " + poreg.getConnection().getRetries() + " retries) while trying to request (ASDU = " + (getRequestASDU()[0] & 0xFF) + "). Aborting.";
                    poreg.getLogger().severe(msg);
                    throw new IOException(msg);
                }
                poreg.getLogger().warning("Resending request (ASDU = " + (getRequestASDU()[0] & 0xFF) + ") (retry " + count + "/" + poreg.getConnection().getRetries() + ")");
            }
        }
    }

    protected void doTheRequest() throws IOException {
        totalReceivedNumberOfRegisters = 0;
        corruptFrame = false;
        corruptCause = "";

        try {
            byte[] result = new byte[0];
            byte[] response = poreg.getConnection().doRequest(getRequestASDU(), getAdditionalBytes(), getExpectedResponseType(), getResponseASDU());

            while (true) {
                validateAdditionalBytes(response);
                response = ProtocolTools.getSubArray(response, getLengthOfReceivedAdditionalBytes());
                result = ProtocolTools.concatByteArrays(result, response);
                if (isCompleted() || isEndOfTable()) {
                    break;
                }
                previouslyReceivedNumberOfRegisters = getReceivedNumberOfRegisters();
                response = poreg.getConnection().doContinue(getExpectedResponseType(), getResponseASDU());
            }

            //Parse the rest
            parse(result);
        } catch (ProtocolConnectionException e) { //E.g. crc error. Do not catch severe IOExceptions
            corruptFrame = true;
            corruptCause = e.getMessage();
        }
    }

    private boolean isEndOfTable() {
        return (getReceivedNumberOfRegisters() < previouslyReceivedNumberOfRegisters);
    }

    private boolean isCompleted() {
        int receivedRegisters = getReceivedRegisterAddress() + getReceivedNumberOfRegisters();
        int receivedFields = getReceivedFieldAddress() + getReceivedNumberOfFields();
        int expectedRegisters = getRegisterAddress() + getNumberOfRegisters();
        int expectedFields = getFieldAddress() + getNumberOfFields();

        return (expectedRegisters == receivedRegisters) && (receivedFields == expectedFields);
    }

    protected byte[] validateAdditionalBytes(byte[] response) throws IOException {
        return response;        //Default there's no additional bytes to validate. Subclasses can override.
    }

    protected abstract int getResponseASDU();

    protected abstract int getExpectedResponseType();

    protected abstract byte[] getRequestASDU();

    protected byte[] getWriteASDU() {
        return new byte[0];     //Only write registers override this
    }

    protected byte[] getAdditionalBytes() throws IOException {
        return new byte[0];     //Default request has no extra info bytes. Subclasses can override.
    }

    protected int getLengthOfReceivedAdditionalBytes() {
        return 0;
    }

    protected byte[] getWriteBytes() {
        return new byte[0];     //Subclasses can override
    }

    public void write() throws IOException {
        byte[] response = poreg.getConnection().doSimpleRequest(getWriteASDU(), getAdditionalBytes(), getWriteBytes());
        parseWriteResponse(response);
    }

    protected int getReceivedNumberOfFields() {
        return receivedNumberOfFields;
    }

    protected int getReceivedFieldAddress() {
        return receivedFieldAddress;
    }

    protected int getReceivedRegisterAddress() {
        return receivedRegisterAddress;
    }

    protected int getReceivedNumberOfRegisters() {
        return receivedNumberOfRegisters;
    }

    protected int getTotalReceivedNumberOfRegisters() {
        return totalReceivedNumberOfRegisters;
    }

    protected int getFieldAddress() {
        return fieldAddress;
    }

    protected int getRegisterAddress() {
        return registerAddress;
    }

    protected int getNumberOfFields() {
        return numberOfFields;
    }

    protected int getNumberOfRegisters() {
        return numberOfRegisters;
    }

}