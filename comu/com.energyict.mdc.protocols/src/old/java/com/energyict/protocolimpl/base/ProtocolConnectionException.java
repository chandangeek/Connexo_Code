/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FlagIEC1107ConnectionException.java
 *
 * Created on 4 oktober 2002, 16:01
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
/**
 *
 * @author  koen
 */
public class ProtocolConnectionException extends ConnectionException {

  private short sReason=-1;;
  private String protocolErrorCode=null;

  public short getReason() {
     return sReason;
  }

  /**
   * Getter for property protocolErrorCode.
   * @return Value of property protocolErrorCode.
   */
  public java.lang.String getProtocolErrorCode() {
      return protocolErrorCode;
  }

  public ProtocolConnectionException(String str) {
      super(str);
  } // public ProtocolConnectionException(String str)

  public ProtocolConnectionException(String str, String protocolErrorCode) {
      super(str);
      this.protocolErrorCode=protocolErrorCode;

  } // public ProtocolConnectionException(String str)

  public ProtocolConnectionException() {
      super();
  } // public ProtocolConnectionException()

  public ProtocolConnectionException(String str, short sReason) {
      super(str,sReason);
      this.sReason = sReason;
  } // public ProtocolConnectionException(String str)

  public ProtocolConnectionException(String str, short sReason, String protocolErrorCode) {
      super(str,sReason);
      this.sReason = sReason;
      this.protocolErrorCode=protocolErrorCode;
  } // public ProtocolConnectionException(String str)


}
