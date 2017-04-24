/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ReadResponse.java
 *
 * Created on 16 februari 2007, 15:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.ConfirmedRespAPSE;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReadResponse extends ConfirmedRespAPSE {

    final int DEBUG=0;

    final int DATA = 0;
    final int DATA_ACCESS_ERROR = 1;

    private byte[] readResponseData;

    /** Creates a new instance of ReadResponse */
    public ReadResponse(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }


    protected byte[] preparebuildPDU() throws IOException {

        return null;
    }

    final int DLMSPDU_READ_RESPONSE=0x0C;

    protected void parsePDU(byte[] data) throws IOException {
        int offset=0;
        if (DEBUG>=1){
        	System.out.println("KV_DEBUG> "+ProtocolUtils.outputHexString(data));
        }

        int length = ProtocolUtils.getInt(data,offset++,1);
        if ((length & 0x80) == 0x80) {
			offset++;
		}

        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag != DLMSPDU_READ_RESPONSE) {
			throw new IOException("ReadResponse, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
		}




        offset++; // skip sequence count
        tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag==DATA_ACCESS_ERROR) {
            int error = ProtocolUtils.getInt(data,offset++,1);
            throw new IOException ("ReadResponse, parsePDU, DataAccessError: "+DataAccessError.getDescription(error));
        }

        setReadResponseData(ProtocolUtils.getSubArray(data,offset));
    }

    public byte[] getReadResponseData() {
        return readResponseData;
    }

    public void setReadResponseData(byte[] readResponseData) {
        this.readResponseData = readResponseData;
    }

}
