/*
 * SEVCIEC1107ConnectionException.java
 *
 * Created on 27 september 2002, 15:26
 */

package com.energyict.protocolimpl.actarissevc;

/**
 *
 * @author  koen
 */
public class SEVCIEC1107ConnectionException extends java.lang.Exception {
    
  private short sReason;
  private static final byte UNKNOWN_ERROR=-1;
  private static final byte TIMEOUT_ERROR=-2;

  public short getReason() {
     return sReason;
  }
  public void setReasonTimeout() {
     sReason = TIMEOUT_ERROR;
  }
  public void setReasonUnknown() {
     sReason = UNKNOWN_ERROR;
  }
  

  public boolean isReasonTimeout() {
      return getReason() == TIMEOUT_ERROR;
  }
  public boolean isReasonUnknown() {
      return getReason() == UNKNOWN_ERROR;
  }
  
  public SEVCIEC1107ConnectionException(String str)
  {
      super(str);
      this.sReason = UNKNOWN_ERROR;
  } // public IEC1107ConnectionException(String str)

  public SEVCIEC1107ConnectionException()
  {
      super();
      this.sReason = UNKNOWN_ERROR;
  } // public IEC1107ConnectionException()
  
  public static SEVCIEC1107ConnectionException getSEVCIEC1107ConnectionExceptionTimeout(String str) {
      SEVCIEC1107ConnectionException e = new SEVCIEC1107ConnectionException(str);
      e.setReasonTimeout();
      return e;
  }
  
  public static SEVCIEC1107ConnectionException getSEVCIEC1107ConnectionExceptionUnknown(String str) {
      SEVCIEC1107ConnectionException e = new SEVCIEC1107ConnectionException(str);
      e.setReasonUnknown();
      return e;
  }
  
  
} // public class IEC1107ConnectionException extends java.lang.Exception {
