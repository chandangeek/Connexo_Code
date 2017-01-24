/*
 * ReadReply.java
 *
 * Created on 1 december 2006, 15:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class ReadReply extends AbstractCommandResponse {

    private int variableAccessSpec; // 8 bit
    private int dataTag; // 8 bit
    private int dataType; // 8 bit
    private byte[] data;

    /** Creates a new instance of ReadReply */
    public ReadReply() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadReply:\n");
        strBuff.append("       data="+ProtocolUtils.outputHexString(getData())+"\n");
        strBuff.append("   dataTag="+getDataTag()+"\n");
        strBuff.append("   dataType="+getDataType()+"\n");
        strBuff.append("   variableAccessSpec="+getVariableAccessSpec()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] rawData) {
        int offset=0;
        offset++; // skip read response

        // READ REPLY
        setVariableAccessSpec((int)rawData[offset++] & 0xff);
        setDataTag((int)rawData[offset++] & 0xff);
        setDataType((int)rawData[offset++] & 0xff);
        if (rawData.length>4)
            setData(ProtocolUtils.getSubArray2(rawData, offset, rawData.length - offset));

//System.out.print("KV_DEBUG> read->parse "+ProtocolUtils.outputHexString(getData()));System.out.println();

    }

    public int getVariableAccessSpec() {
        return variableAccessSpec;
    }

    public void setVariableAccessSpec(int variableAccessSpec) {
        this.variableAccessSpec = variableAccessSpec;
    }

    public int getDataTag() {
        return dataTag;
    }

    public void setDataTag(int dataTag) {
        this.dataTag = dataTag;
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
