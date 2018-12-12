/*
 * InitiateResponse.java
 *
 * Created on 4 december 2006, 15:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class InitiateResponse extends AbstractCommandResponse {
    
   private int negotiatedVersion; // 0x03 from trace file
   private int negotiatedConformance; // 0x000f from trace file
   private int negoiatedMaxPDUSize; // 0x01A4 from trace file
   private int vaaName; // 0x0000 from trace file
   private int qualityOfService; // 0x00 from trace file
   
    
    /** Creates a new instance of WriteReply */
    public InitiateResponse() {
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("InitiateResponse:\n");
        strBuff.append("   negotiatedVersion=0x"+Integer.toHexString(getNegotiatedVersion())+"\n");
        strBuff.append("   negotiatedConformance=0x"+Integer.toHexString(getNegotiatedConformance())+"\n");
        strBuff.append("   negoiatedMaxPDUSize=0x"+Integer.toHexString(getNegoiatedMaxPDUSize())+"\n");
        strBuff.append("   vaaName="+getVaaName()+"\n");
        strBuff.append("   qualityOfService="+getQualityOfService()+"\n");
        return strBuff.toString();
    }
    
    protected void parse(byte[] rawData) throws IOException {
        int offset = 0;
        offset++; // skip read response
        setNegotiatedVersion((int)rawData[offset++]&0xFF);
        setNegotiatedConformance(ProtocolUtils.getInt(rawData,offset,2));
        offset+=2;
        setNegoiatedMaxPDUSize(ProtocolUtils.getInt(rawData,offset,2)); 
        offset+=2;
        setVaaName(ProtocolUtils.getInt(rawData,offset,2)); 
        offset+=2;
        setQualityOfService((int)rawData[offset++]&0xFF);
    }

    public int getNegotiatedVersion() {
        return negotiatedVersion;
    }

    public void setNegotiatedVersion(int negotiatedVersion) {
        this.negotiatedVersion = negotiatedVersion;
    }

    public int getNegotiatedConformance() {
        return negotiatedConformance;
    }

    public void setNegotiatedConformance(int negotiatedConformance) {
        this.negotiatedConformance = negotiatedConformance;
    }

    public int getNegoiatedMaxPDUSize() {
        return negoiatedMaxPDUSize;
    }

    public void setNegoiatedMaxPDUSize(int negoiatedMaxPDUSize) {
        this.negoiatedMaxPDUSize = negoiatedMaxPDUSize;
    }

    public int getVaaName() {
        return vaaName;
    }

    public void setVaaName(int vaaName) {
        this.vaaName = vaaName;
    }

    public int getQualityOfService() {
        return qualityOfService;
    }

    public void setQualityOfService(int qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

}