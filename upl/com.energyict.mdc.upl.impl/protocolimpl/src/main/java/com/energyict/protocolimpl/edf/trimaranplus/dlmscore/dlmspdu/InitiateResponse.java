/*
 * InitiateResponse.java
 *
 * Created on 15 februari 2007, 16:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.dlmscore.dlmspdu;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.*;
import java.io.*;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.InitiateRespAPSE;

/**
 *
 * @author Koen
 */
public class InitiateResponse extends InitiateRespAPSE {

    // Due to the fact that the encoding of initiateresponse does not seems to
    // be conform with the A-XDR or BER standard, i hereby only save the data in an array for future use...
    byte[] initiateResponseData;
    
    /** Creates a new instance of InitiateResponse */
    public InitiateResponse(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }
 
    protected byte[] preparebuildPDU() throws IOException {
        return null;
    }
    
    final int DLMSPDU_INITIATE_RESPONSE=8;
    
    protected void parsePDU(byte[] data) throws IOException {
        
        int offset=0;
        offset++; // skip length
        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag != DLMSPDU_INITIATE_RESPONSE)
            throw new IOException("InitiateResponse, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
        
        initiateResponseData = ProtocolUtils.getSubArray(data,offset);
        
//        $0a 10 bytes following $08 --> DLMSPDU InitiateResponse See 61334-4-41 page 207
//							  $00  IMPLICIT integer 8 optional 
//							  $50$10     negotiated dlms version nr
//							  $18$00  Conformance
//							  $0e$af  negotiated max PDU size
//							  $10$07  vaa name 
    }
    
}
