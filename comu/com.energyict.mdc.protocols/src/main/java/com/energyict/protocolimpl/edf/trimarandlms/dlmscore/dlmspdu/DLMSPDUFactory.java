/*
 * DLMSPDUFactory.java
 *
 * Created on 15 februari 2007, 16:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DLMSPDUFactory {

    private ProtocolLink protocolLink;

    StatusResponse statusResponse = null;

    /** Creates a new instance of DLMSPDUFactory */
    public DLMSPDUFactory(ProtocolLink protocolLink) {
        this.setProtocolLink(protocolLink);
    }



    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public void setProtocolLink(ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
    }

    public StatusResponse getStatusResponse() throws IOException {
        if (statusResponse == null) {
            StatusRequest obj = new StatusRequest(this);
            obj.setIdentify(true);
            obj.invoke();
            statusResponse = obj.getStatusResponse();
        }
        return statusResponse;
    }

    public InitiateRequest getInitiateRequest() throws IOException {
        InitiateRequest initiateRequest = new InitiateRequest(this);
        initiateRequest.invoke();
        return initiateRequest;
    }

    public ReadRequest getReadRequest(int variableName) throws IOException {
        ReadRequest readRequest = new ReadRequest(this);
        readRequest.setVariableName(variableName);
        readRequest.invoke();
        return readRequest;
    }
    public WriteRequest getWriteRequest(int variableName, byte[] data) throws IOException {
        WriteRequest writeRequest = new WriteRequest(this);
        writeRequest.setVariableName(variableName);
        writeRequest.setData(data);
        writeRequest.invoke();
        return writeRequest;
    }
}
