/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ResponseData.java
 *
 * Created on 20 maart 2006, 17:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom;

import com.energyict.protocols.util.ProtocolUtils;

/**
 *
 * @author koen
 */
public class ResponseData {


    private byte[] data;

    /** Creates a new instance of ResponseData */
    public ResponseData(byte[] data) {
        this.setData(data);
    }

    public String toString() {
        return "ResponseData binary: "+ProtocolUtils.outputHexString(getData())+"\n"+"ResponseData ascii: "+new String(getData());
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }
}
