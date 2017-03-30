/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * VDEWException.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

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
