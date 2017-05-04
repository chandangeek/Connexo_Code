/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IEC870ConnectionException.java
 *
 * Created on 19 juni 2003, 11:32
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

/**
 *
 * @author  Koen
 */
public class IEC870ConnectionException extends ConnectionException {

  private short sReason;

  public short getReason()
  {
     return sReason;
  }

  public IEC870ConnectionException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public IEC870ConnectionException(String str)

  public IEC870ConnectionException()
  {
      super();
      this.sReason = -1;

  } // public IEC870ConnectionException()

  public IEC870ConnectionException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public IEC870ConnectionException(String str)

}