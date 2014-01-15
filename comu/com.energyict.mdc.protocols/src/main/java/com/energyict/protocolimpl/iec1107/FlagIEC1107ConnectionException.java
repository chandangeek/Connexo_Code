/*
 * FlagIEC1107ConnectionException.java
 *
 * Created on 4 oktober 2002, 16:01
 */

package com.energyict.protocolimpl.iec1107;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
/**
 *
 * @author  koen
 */
public class FlagIEC1107ConnectionException extends ConnectionException {

  private short sReason;

  public short getReason() {
     return sReason;
  }

  public FlagIEC1107ConnectionException(String str) {
      super(str);
      this.sReason = -1;
  } // public IEC1107ConnectionException(String str)

  public FlagIEC1107ConnectionException() {
      super();
      this.sReason = -1;
  } // public IEC1107ConnectionException()

  public FlagIEC1107ConnectionException(String str, short sReason) {
      super(str,sReason);
      this.sReason = sReason;

  } // public IEC1107ConnectionException(String str)

}
