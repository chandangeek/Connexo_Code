/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UploadSegmentResponse.java
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
public class UploadSegmentResponse extends AbstractCommandResponse {

    private int responseID;
    private int segmentNr;
    private int dataType;
    private byte[] data;

    /** Creates a new instance of UploadSegmentResponse */
    public UploadSegmentResponse() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("UploadSegmentResponse:\n");
        for (int i=0;i<getData().length;i++) {
            strBuff.append("       data["+i+"]="+getData()[i]+"\n");
        }
        strBuff.append("   dataType="+getDataType()+"\n");
        strBuff.append("   responseID="+getResponseID()+"\n");
        strBuff.append("   segmentNr="+getSegmentNr()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] rawData) throws IOException {
        int offset = 0;
        offset++; // skip confirmed service response
        setResponseID((int)rawData[offset++]&0xFF);
        setSegmentNr(ProtocolUtils.getInt(rawData,offset,2));
        offset+=2;
        setDataType((int)rawData[offset++]&0xFF);
        setData(ProtocolUtils.getSubArray2(rawData,offset, rawData.length-offset));

     }

    public int getResponseID() {
        return responseID;
    }

    public void setResponseID(int responseID) {
        this.responseID = responseID;
    }

    public int getSegmentNr() {
        return segmentNr;
    }

    public void setSegmentNr(int segmentNr) {
        this.segmentNr = segmentNr;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
