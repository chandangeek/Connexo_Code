/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InitiateRespAPSE.java
 *
 * Created on 15 februari 2007, 15:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class InitiateRespAPSE extends AbstractAPSEPDU {

    abstract protected byte[] preparebuildPDU() throws IOException;
    abstract protected void parsePDU(byte[] data) throws IOException;

    private int negotiatedAppCtxName;

    /** Creates a new instance of InitiateRespAPSE */
    public InitiateRespAPSE(APSEPDUFactory aPSEFactory) {
        super(aPSEFactory);
    }

    final int INITIATE_RESP_APSE = 7;

    byte[] preparebuild() throws IOException {
        return null;
    }

    void parse(byte[] data) throws IOException {
        int offset=0;
        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag != INITIATE_RESP_APSE){
            throw new IOException("InitiateRespAPSE, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
        }
        negotiatedAppCtxName = ProtocolUtils.getInt(data,offset++,1);
        parsePDU(ProtocolUtils.getSubArray(data,offset));
    }

    private int getNegotiatedAppCtxName() {
        return negotiatedAppCtxName;
    }

    public void setNegotiatedAppCtxName(int negotiatedAppCtxName) {
        this.negotiatedAppCtxName = negotiatedAppCtxName;
    }

}
