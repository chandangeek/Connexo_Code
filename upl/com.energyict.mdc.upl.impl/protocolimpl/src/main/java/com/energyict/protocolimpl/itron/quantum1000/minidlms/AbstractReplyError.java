/*
 * AbstractReplyError.java
 *
 * Created on 4 december 2006, 13:58
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
abstract public class AbstractReplyError extends AbstractCommandResponse {
    
    private int error;
    private int dataAccessError;
    
    
    private static final int SCOPE_ACCESS_VIOLATED = 0x00;
    private static final int OBJECT_UNAVAILABLE    = 0x01;
    private static final int HDW_FAILURE           = 0x02;
    private static final int TEMPORARY_FAILURE     = 0x03;
    private static final int TYPE_UNMATCHED        = 0x04;      
    private static final int OBJECT_UNDEFINED      = 0x05;
    private static final int OBJECT_CLASS_UNCONSISTENT = 0x06;
    private static final int READ_WRITE_DENIED     = 0x07;
    private static final int WRITE_DATA_INVALID_ERROR    = 0x08;
    private static final int INDEX_INVALID_ERROR    = 0x09;    
    private static final int WRITE_PROCESS_ERROR    = 0x0A;       
    
    private static final int INITIATE_UPLOAD_ERROR = 0x11;
    private static final int UPLOAD_SEGMENT_ERROR  = 0x12;
    private static final int TERMINTE_UPLOAD_ERROR = 0x13;    
    
    static final String[] errorStrings = new String[] {"SCOPE_ACCESS_VIOLATED", // 0
                                          "OBJECT_UNAVAILABLE",    // 1
                                          "HDW_FAILURE",           // 2  
                                          "TEMPORARY_FAILURE",     // 3
                                          "TYPE_UNMATCHED",        // 4 
                                          "OBJECT_UNDEFINED",      // 5 
                                          "OBJECT_CLASS_UNCONSISTENT",  // 6
                                          "READ_WRITE_DENIED",     // 7      
                                          "WRITE_DATA_INVALID_ERROR", // 8
                                          "INDEX_INVALID_ERROR", // 9
                                          "WRITE_PROCESS_ERROR", // A
                                          "", // B
                                          "", // C
                                          "", // D
                                          "", // E
                                          "", // F
                                          "", // 10
                                          "INITIATE_UPLOAD_ERROR", // 11
                                          "UPLOAD_SEGMENT_ERROR", // 12
                                          "TERMINTE_UPLOAD_ERROR"}; // 13
    
    /**
     * Creates a new instance of AbstractReplyError 
     */
    public AbstractReplyError() {
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("WriteReplyDataError:\n");
        strBuff.append("   error="+getError()+", "+errorStrings[getError()]+"\n");
        strBuff.append("   dataAccessError="+getDataAccessError()+"\n");
        return strBuff.toString();
    }
    
    protected void parse(byte[] rawData) {
        int offset = 0;
        offset++; // skip read response
        setDataAccessError((int)rawData[offset++] & 0xff);
        setError((int)rawData[offset++] & 0xff);
    }
    
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }    
    
    public int getDataAccessError() {
        return dataAccessError;
    }

    public void setDataAccessError(int dataAccessError) {
        this.dataAccessError = dataAccessError;
    }

    public static int getSCOPE_ACCESS_VIOLATED() {
        return SCOPE_ACCESS_VIOLATED;
    }

    public static int getOBJECT_UNAVAILABLE() {
        return OBJECT_UNAVAILABLE;
    }

    public static int getHDW_FAILURE() {
        return HDW_FAILURE;
    }

    public static int getTEMPORARY_FAILURE() {
        return TEMPORARY_FAILURE;
    }

    public static int getTYPE_UNMATCHED() {
        return TYPE_UNMATCHED;
    }

    public static int getOBJECT_UNDEFINED() {
        return OBJECT_UNDEFINED;
    }

    public static int getOBJECT_CLASS_UNCONSISTENT() {
        return OBJECT_CLASS_UNCONSISTENT;
    }

    public static int getREAD_WRITE_DENIED() {
        return READ_WRITE_DENIED;
    }

    public static int getWRITE_DATA_INVALID_ERROR() {
        return WRITE_DATA_INVALID_ERROR;
    }

    public static int getINDEX_INVALID_ERROR() {
        return INDEX_INVALID_ERROR;
    }

    public static int getWRITE_PROCESS_ERROR() {
        return WRITE_PROCESS_ERROR;
    }
    
    public static int getINITIATE_UPLOAD_ERROR() {
        return INITIATE_UPLOAD_ERROR;
    }

    public static int getUPLOAD_SEGMENT_ERROR() {
        return UPLOAD_SEGMENT_ERROR;
    }

    public static int getTERMINTE_UPLOAD_ERROR() {
        return TERMINTE_UPLOAD_ERROR;
    }    
    
    public boolean isInvalidObject() {
        return ((getError() == getOBJECT_UNAVAILABLE()) || (getError() == getOBJECT_UNDEFINED()));
    }
}
