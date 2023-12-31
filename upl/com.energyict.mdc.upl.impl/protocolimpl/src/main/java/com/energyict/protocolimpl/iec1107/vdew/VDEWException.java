/*
 * VDEWException.java
 *
 * Created on 4 oktober 2002, 16:01
 */

package com.energyict.protocolimpl.iec1107.vdew;

import java.io.IOException;
/**
 *
 * @author  koen
 */
public class VDEWException extends IOException {
    
  private short sReason;

  public short getReason() {
     return sReason;
  }
   
  public VDEWException(String str) {
      super(str);
      this.sReason = -1;
  } // public VDEWException(String str)

  public VDEWException() {
      super();
      this.sReason = -1;
  } // public VDEWException()

  public VDEWException(String str, short sReason) {
      super(str);
      this.sReason = sReason;

  } // public VDEWException(String str)    
    
}
