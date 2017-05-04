/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CIField72h.java
 *
 * Created on 3 oktober 2007, 13:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class CIField7Ah extends AbstractCIField {

    private int accessNumber;
    private int statusByte;
    private int signatureField;


    /** Creates a new instance of CIField72h */
    public CIField7Ah() {
    }

    protected int getId() {
        return 0x7A;
    }

    public String toString() {
        return "CIField7Ah:\n" +
                "   accessNumber=" + getAccessNumber() + "\n" +
                "   statusByte=" + getStatusByte() + "\n" +
                "   signatureField=" + getSignatureField() + "\n";
    }

    protected void doParse(byte[] data) throws IOException {
        int offset=0;
        setAccessNumber(ProtocolUtils.getIntLE(data,offset++,1));
        setStatusByte(ProtocolUtils.getIntLE(data,offset++,1));
        setSignatureField(ProtocolUtils.getIntLE(data,offset,2));
        offset+=2;
    }

    public int getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(int accessNumber) {
        this.accessNumber = accessNumber;
    }

    public int getStatusByte() {
        return statusByte;
    }

    public void setStatusByte(int statusByte) {
        this.statusByte = statusByte;
    }

    public int getSignatureField() {
        return signatureField;
    }

    public void setSignatureField(int signatureField) {
        this.signatureField = signatureField;
    }

}
