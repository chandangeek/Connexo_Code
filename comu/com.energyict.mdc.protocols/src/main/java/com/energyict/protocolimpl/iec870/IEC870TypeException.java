/*
 * IEC870TypeException.java
 *
 * Created on 1 juli 2003, 9:06
 */

package com.energyict.protocolimpl.iec870;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class IEC870TypeException extends IOException {

  private short sReason;

  public short getReason()
  {
     return sReason;
  }

  public IEC870TypeException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public IEC870ConnectionException(String str)

  public IEC870TypeException()
  {
      super();
      this.sReason = -1;

  } // public IEC870ConnectionException()

  public IEC870TypeException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public IEC870TypeException(String str)
}
