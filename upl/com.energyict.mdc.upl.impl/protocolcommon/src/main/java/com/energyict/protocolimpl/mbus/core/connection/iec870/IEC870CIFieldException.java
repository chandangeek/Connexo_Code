/*
 * IEC870CIFieldException.java
 *
 * Created on 1 juli 2003, 9:06
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class IEC870CIFieldException extends IOException {
    
  private short sReason;

  public short getReason()
  {
     return sReason;
  }
   
  public IEC870CIFieldException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public IEC870ConnectionException(String str)

  public IEC870CIFieldException()
  {
      super();
      this.sReason = -1;
      
  } // public IEC870ConnectionException()

  public IEC870CIFieldException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public IEC870CIFieldException(String str)    
}
