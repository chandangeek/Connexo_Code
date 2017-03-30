/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PACTConnectionException.java
 *
 * Created on 26 maart 2004, 16:40
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
/**
 *
 * @author  Koen
 */
public class PACTConnectionException extends ConnectionException {
  private short sReason;

  public short getReason() {
     return sReason;
  }

  public PACTConnectionException(String str) {
      super(str);
      this.sReason = -1;
  } // public PACTConnectionException(String str)

  public PACTConnectionException() {
      super();
      this.sReason = -1;
  } // public PACTConnectionException()

  public PACTConnectionException(String str, short sReason) {
      super(str,sReason);
      this.sReason = sReason;

  } // public PACTConnectionException(String str, short sReason)

}
