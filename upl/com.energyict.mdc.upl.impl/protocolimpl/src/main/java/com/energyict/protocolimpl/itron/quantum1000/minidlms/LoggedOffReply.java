/*
 * WriteReply.java
 *
 * Created on 1 december 2006, 16:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class LoggedOffReply extends AbstractCommandResponse {
    
   private int type;
   private int vdeStateError;
   private int noDLMSContextError;
   
    
    /** Creates a new instance of WriteReply */
    public LoggedOffReply() {
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoggedOffReply:\n");
        strBuff.append("   noDLMSContextError="+getNoDLMSContextError()+"\n");
        strBuff.append("   type="+getType()+"\n");
        strBuff.append("   vdeStateError="+getVdeStateError()+"\n");
        return strBuff.toString();
    }
    
    protected void parse(byte[] rawData) {
        int offset = 0;
        offset++; // skip read response
        setType((int)rawData[offset++]&0xFF);
        setVdeStateError((int)rawData[offset++]&0xFF);
        setNoDLMSContextError((int)rawData[offset++]&0xFF);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVdeStateError() {
        return vdeStateError;
    }

    public void setVdeStateError(int vdeStateError) {
        this.vdeStateError = vdeStateError;
    }

    public int getNoDLMSContextError() {
        return noDLMSContextError;
    }

    public void setNoDLMSContextError(int noDLMSContextError) {
        this.noDLMSContextError = noDLMSContextError;
    }


}
