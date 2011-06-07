package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;

import java.io.IOException;

/**
 * Parent class for all requests.
 * Contains common methods, e.g. doRequest()
 *
 * Copyrights EnergyICT
 * Date: 21-apr-2011
 * Time: 22:02:44
 */
abstract public class AbstractRequest {

    protected Poreg poreg;

    public AbstractRequest(Poreg poreg) {
        this.poreg = poreg;
    }

    protected abstract void parse(byte[] data) throws IOException;

    protected void parseWriteResponse(byte[] data) throws IOException {
        if (data.length == 3) {
            if ((data[0] & 0xFF) == Response.ACK.getId()) {
                return;
            }
        }
        throw new IOException("Error writing register, expected ACK, received " + Response.getDescription(data[0] & 0xFF));
    }

    public void doRequest() throws IOException {
        byte[] response = poreg.getConnection().doRequest(getRequestASDU(), getAdditionalBytes(), getExpectedResponseType(), getResponseASDU());
        response = validateAdditionalBytes(response);

        //Parse the rest
        parse(response);
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
}