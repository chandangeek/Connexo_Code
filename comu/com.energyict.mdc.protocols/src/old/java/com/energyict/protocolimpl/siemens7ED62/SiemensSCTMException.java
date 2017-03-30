/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SiemensSCTMException.java
 *
 * Created on 24 januari 2003, 14:44
 */

package com.energyict.protocolimpl.siemens7ED62;

/**
 *
 * @author  Koen
 */
public class SiemensSCTMException extends java.lang.Exception {
    
  private short sReason;

  public short getReason()
  {
     return sReason;
  }
   
  public SiemensSCTMException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public SiemensSCTMException(String str)

  public SiemensSCTMException()
  {
      super();
      this.sReason = -1;
      
  } // public SiemensSCTMException()

  public SiemensSCTMException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public SiemensSCTMException(String str)    
    
}
