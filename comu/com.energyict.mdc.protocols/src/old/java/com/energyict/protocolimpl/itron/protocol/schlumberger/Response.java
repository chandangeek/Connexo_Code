/*
 * Response.java
 *
 * Created on 8 september 2006, 11:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author Koen
 */
public class Response {

    private byte[] data;

    /** Creates a new instance of Response */
    public Response(byte[] data) {
        this.setData(data);
    }

    public String toString() {
        return ProtocolUtils.outputHexString(data);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}
