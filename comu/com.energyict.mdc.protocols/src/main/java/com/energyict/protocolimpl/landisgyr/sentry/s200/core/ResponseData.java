/*
 * ResponseData.java
 *
 * Created on 26 juli 2006, 16:19
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

/**
 *
 * @author Koen
 */
public class ResponseData {
    
    private byte[] data;
    private StatusByte status;
    
    /** Creates a new instance of ResponseData */
    public ResponseData(ResponseFrame responseFrame) {
        setData(responseFrame.getData());
        status = responseFrame.getStatus();
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public StatusByte getStatus() {
        return status;
    }

    private void setStatus(StatusByte status) {
        this.status = status;
    }
    
}
