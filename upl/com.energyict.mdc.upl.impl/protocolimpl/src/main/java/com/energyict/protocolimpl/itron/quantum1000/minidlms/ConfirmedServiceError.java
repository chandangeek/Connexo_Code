/*
 * ConfirmedServiceError.java
 *
 * Created on 4 december 2006, 16:11
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
public class ConfirmedServiceError extends AbstractReplyError {
    
    private int error;
    
//    private static final int INITIATE_UPLOAD_ERROR = 0x11;
//    private static final int UPLOAD_SEGMENT_ERROR  = 0x12;
//    private static final int TERMINTE_UPLOAD_ERROR = 0x13;
    
    /** Creates a new instance of ConfirmedServiceError */
    public ConfirmedServiceError() {
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ConfirmedServiceError:\n");
        strBuff.append("   error="+getError()+"\n");
        return strBuff.toString();
    }
    
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new ConfirmedServiceError()));
//    }
    
    protected void parse(byte[] rawData) {
        int offset = 0;
        offset++; // skip read response
        setError((int)rawData[offset++] & 0xff);
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }


    
}
