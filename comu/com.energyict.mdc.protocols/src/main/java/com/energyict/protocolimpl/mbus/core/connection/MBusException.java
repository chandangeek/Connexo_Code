/*
 * MBusException.java
 *
 * Created on 20 september 2005, 10:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core.connection;

import java.io.IOException;

/**
 *
 * @author Koen
 */


public class MBusException extends IOException {
    
  private short sReason;

  public short getReason() {
     return sReason;
  }
   
  public MBusException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public MBusException(String str)

  public MBusException()
  {
      super();
      this.sReason = -1;
      
  } // public MBusException()

//  public MBusException(String str, short sReason)
//  {
//      super(str);
//      this.sReason = sReason;
//
//  } // public MBusException(String str)    
}
