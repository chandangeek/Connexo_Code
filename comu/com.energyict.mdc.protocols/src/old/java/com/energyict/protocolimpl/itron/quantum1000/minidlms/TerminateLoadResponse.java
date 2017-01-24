/*
 * TerminateLoadResponse.java
 *
 * Created on 4 december 2006, 16:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TerminateLoadResponse extends AbstractCommandResponse {

    private int responseID;

    /** Creates a new instance of TerminateLoadResponse */
    public TerminateLoadResponse() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TerminateLoadResponse:\n");
        strBuff.append("   responseID="+getResponseID()+"\n");
        return strBuff.toString();
    }


    protected void parse(byte[] rawData) throws IOException {
        int offset = 0;
        offset++; // skip confirmed service response
        setResponseID((int)rawData[offset++]&0xFF);
     }

    public int getResponseID() {
        return responseID;
    }

    public void setResponseID(int responseID) {
        this.responseID = responseID;
    }

}
