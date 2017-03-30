/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ResponseFrame.java
 *
 * Created on 20 maart 2006, 17:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.core;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author koen
 */
public class ResponseFrame {

    private byte[] data;
    private int type;
    private int nSEQRx;


    /** Creates a new instance of ResponseFrame */
    public ResponseFrame(byte[] data, int type, int nSEQRx) {
        this.setData(data);
        this.setType(type);
        this.setNSEQRx(nSEQRx);
    }

    public String toString() {
        return "ResponseFrame binary: "+ProtocolUtils.outputHexString(getData())+"\n"+"ResponseFrame ascii: "+new String(getData()+", type="+type+", nSEQRx="+nSEQRx);
    }



    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNSEQRx() {
        return nSEQRx;
    }

    public void setNSEQRx(int nSEQRx) {
        this.nSEQRx = nSEQRx;
    }
}
