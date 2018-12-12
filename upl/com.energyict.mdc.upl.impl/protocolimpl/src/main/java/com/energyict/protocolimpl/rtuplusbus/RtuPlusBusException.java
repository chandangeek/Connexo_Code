/*
 * RtuPlusBusException.java
 *
 * Created on 12 februari 2003, 16:52
 */

package com.energyict.protocolimpl.rtuplusbus;

/**
 *
 * @author  Stefan Grosjean
 */

public class RtuPlusBusException extends java.lang.Exception {
    
    /** for errors in crc and heading */
    public final static int CHECKSUM = 1;           
    /** for timeout */
    public final static int TIME_OUT_ERROR = 2;         
    /** for server-id mismatch */
    public final static int SERVER_ID_SMALL = 3;         
    /** the complement of the destination is wrong */
    public final static int DEST_COMPLEMENT = 4;     
    /** the complement of the source is wrong */
    public final static int SRC_COMPLEMENT = 5;     
    /** the complement of the data size is wrong */
    public final static int DATA_SIZE_COMPLEMENT = 6;    
     
    int reason = 0;
    
    /**
     * Creates a new instance of <code>RtuPlusBusException</code> without detail message.
     */
    public RtuPlusBusException() 
    {
    }
    
    /**
     * Creates a new instance of <code>RtuPlusBusException</code> with a reason.
     */
    public RtuPlusBusException( int reason )
    {   this.reason = reason;
    }
    
    public RtuPlusBusException( String msg, int reason )
    {   super( msg );
        this.reason = reason;
    }
    
    /**
     * Constructs an instance of <code>RtuPlusBusException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RtuPlusBusException(String msg) 
    {  super(msg);
    }
    
    public RtuPlusBusException(String msg, Throwable cause){
        super(msg, cause);
    }
    

    public boolean equals(Object obj) 
    { return super.equals( obj);
    }
    
    public String toString() 
    { return super.toString();
    }
    
    public String getLogMessage( ){    
        switch( getReason() ) {
            case CHECKSUM:          // for errors in frame checksum
                return "CHECKSUM error (frame checksum)";       
            case TIME_OUT_ERROR:    // for timeout
                return "TIMEOUT error";        
            case SERVER_ID_SMALL:   // for server-id mismatch
                return "SERVER ID mismatch";       
            case DEST_COMPLEMENT:   
                return "Complement of destination address is wrong";
            case SRC_COMPLEMENT:    
                return "Complement of source address is wrong";   
            case DATA_SIZE_COMPLEMENT:    
                return "Complement of data size";       
            default:                
                return "unknown";
        }   
    }   
    
    public Throwable getCause() 
    { return super.getCause();
    }
    
    public String getLocalizedMessage() 
    { return super.getLocalizedMessage();
    }
 
    public Throwable initCause(Throwable cause) 
    { return super.initCause( cause );
    }
    
    public int getReason( ){
        return reason;
    }
}
