/*
 * RequestInfo.java
 *
 * Created on 17 oktober 2005, 10:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class RequestData {

    private int code;
    private byte[] data;


    /** Creates a new instance of RequestInfo */
    public RequestData() {
        this(-1);
    }
    public RequestData(int code) {
        this(code,null);
    }
    public RequestData(int code,byte[] data) {
        this.setCode(code);
        this.data=data;
    }

    public String toString() {
        return "RequestData: code="+code+(data==null?"":", data="+ProtocolUtils.outputHexString(data));
    }

    public byte[] getAssembledData() {
        if (getData() == null)
            return new byte[]{(byte)getCode()};
        else {
            byte[] assembledData = new byte[getData().length+1];
            System.arraycopy(getData(),0,assembledData,1,getData().length);
            assembledData[0]=(byte)getCode();
            return assembledData;
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
