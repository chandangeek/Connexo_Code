/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConfirmedRespAPSE.java
 *
 * Created on 16 februari 2007, 14:57
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
abstract public class ConfirmedRespAPSE extends AbstractAPSEPDU {

    final int DEBUG=0;

    abstract protected byte[] preparebuildPDU() throws IOException;
    abstract protected void parsePDU(byte[] data) throws IOException;


    /** Creates a new instance of ConfirmedRespAPSE */
    public ConfirmedRespAPSE(APSEPDUFactory aPSEPDUFactory) {
        super(aPSEPDUFactory);
    }

    final int CONFIRMED_RESP_APSE = 1;
    final int CONFIRMED_ERROR_APSE = 2;

    byte[] preparebuild() throws IOException {
        return null;
    }

    void parse(byte[] data) throws IOException {

        if (DEBUG>=1){
        	System.out.println("KV_DEBUG> "+ProtocolUtils.outputHexString(data));
        }
        int offset=0;
        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag == CONFIRMED_ERROR_APSE) {
			throw new IOException("ConfirmedRespAPSE, parse, ConfirmedErrorAPSE received!");
		}
        if (tag != CONFIRMED_RESP_APSE) {
			throw new IOException("ConfirmedRespAPSE, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
		}
        parsePDU(ProtocolUtils.getSubArray(data,offset));
    }
}
