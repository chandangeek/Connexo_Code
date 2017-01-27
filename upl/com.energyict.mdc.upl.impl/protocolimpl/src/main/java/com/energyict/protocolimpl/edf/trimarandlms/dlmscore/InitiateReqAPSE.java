/*
 * AuthenticationReqAPSE.java
 *
 * Created on 15 februari 2007, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.InitiateResponse;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class InitiateReqAPSE extends AbstractAPSEPDU {
    
    abstract protected byte[] preparebuildPDU() throws IOException;
    abstract protected void parsePDU(byte[] data) throws IOException;
    
    
    private InitiateRespAPSE initiateRespAPSE=null;
    
    
    /** Creates a new instance of AuthenticationReqAPSE */
    public InitiateReqAPSE(APSEPDUFactory aPSEFactory) {
        super(aPSEFactory);
    }
    
    final int INITIATE_REQ_APSE = 6;
    
    byte[] preparebuild() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        baos.write(INITIATE_REQ_APSE);
        baos.write(0x80);
        baos.write(0x40);
        baos.write(getAPSEFactory().getAPSEParameters().getCipheredServerRandom());
        baos.write(getAPSEFactory().getAPSEParameters().getProposedAppCtxName());
        byte[] converted = ProtocolUtils.convert2ascii(getAPSEFactory().getAPSEParameters().getCallingPhysicalAddress().getBytes());
        baos.write(converted.length);
        baos.write(converted);
        baos.write(preparebuildPDU());
        return baos.toByteArray();
    }
    
    void parse(byte[] data) throws IOException {
        setInitiateRespAPSE(new InitiateResponse(getAPSEFactory().getProtocolLink().getDLMSPDUFactory()));
        getInitiateRespAPSE().parse(data);
    }

    public InitiateRespAPSE getInitiateRespAPSE() {
        return initiateRespAPSE;
    }

    private void setInitiateRespAPSE(InitiateRespAPSE initiateRespAPSE) {
        this.initiateRespAPSE = initiateRespAPSE;
    }


}
