/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InitiateRequest.java
 *
 * Created on 15 februari 2007, 16:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.InitiateReqAPSE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class InitiateRequest extends InitiateReqAPSE {

    private InitiateResponse initiateResponse;

    /** Creates a new instance of InitiateRequest */
    public InitiateRequest(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }

    final int DLMSPDU_INITIATE_REQUEST=1;

    // The problem here is that none of the documents & standards i have can expplain how the coding of the InitiateRequest DLMS PDU is done
    // In the idea that this does not change, herby the hardcoded frame taken from a trace between STAR software and a Trimaran+ meter
    // 9 bytes
    final byte[] HARDCODED_INITIATE_REQUEST=new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x50,(byte)0x10,(byte)0x1c,(byte)0x28,(byte)0x78,(byte)0x00};

    protected byte[] preparebuildPDU() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(10); // length
        baos.write(DLMSPDU_INITIATE_REQUEST);
        baos.write(HARDCODED_INITIATE_REQUEST);
        return baos.toByteArray();
    }

    protected void parsePDU(byte[] data) throws IOException {
        setInitiateResponse(new InitiateResponse(getAPSEFactory().getProtocolLink().getDLMSPDUFactory()));
        getInitiateResponse().parsePDU(data);
    }

    public InitiateResponse getInitiateResponse() {
        return initiateResponse;
    }

    public void setInitiateResponse(InitiateResponse initiateResponse) {
        this.initiateResponse = initiateResponse;
    }

}
