/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InitiateUploadResponse.java
 *
 * Created on 4 december 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class InitiateUploadResponse extends AbstractCommandResponse {

    private int responseID;
    private int nrOfSegments;

    /** Creates a new instance of InitiateUploadResponse */
    public InitiateUploadResponse() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InitiateUploadResponse:\n");
        strBuff.append("   nrOfSegments="+getNrOfSegments()+"\n");
        strBuff.append("   responseID="+getResponseID()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] rawData) throws IOException {
        int offset = 0;
        offset++; // skip confirmed service response
        setResponseID((int)rawData[offset++]&0xFF);
        setNrOfSegments(ProtocolUtils.getInt(rawData,offset,2));
        offset+=2;
     }

    public int getResponseID() {
        return responseID;
    }

    public void setResponseID(int responseID) {
        this.responseID = responseID;
    }

    public int getNrOfSegments() {
        return nrOfSegments;
    }

    public void setNrOfSegments(int nrOfSegments) {
        this.nrOfSegments = nrOfSegments;
    }

}
