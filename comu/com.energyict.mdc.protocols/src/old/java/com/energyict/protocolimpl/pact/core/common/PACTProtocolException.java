/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PACTProtocolException.java
 *
 * Created on 12 maart 2004, 10:47
 */

package com.energyict.protocolimpl.pact.core.common;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class PACTProtocolException extends IOException {

  private short sReason;

  public short getReason()
  {
     return sReason;
  }

  public PACTProtocolException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public PACTProtocolException(String str)

  public PACTProtocolException()
  {
      super();
      this.sReason = -1;

  } // public PACTProtocolException()

  public PACTProtocolException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public PACTProtocolException(String str)
}
