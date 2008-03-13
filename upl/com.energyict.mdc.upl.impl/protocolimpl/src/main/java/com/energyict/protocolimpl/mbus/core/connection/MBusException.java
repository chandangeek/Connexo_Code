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

import java.io.*;

/**
 *
 * @author Koen
 */


public class MBusException extends IOException {

  static public final short GENERAL_ERROR=0;  
  static public final short PARSE_ERROR=1;  
  static public final short PARSE_LENGTH_ERROR=2;  
  static public final short WRITE_ERROR=3;  
    
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

  public MBusException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public MBusException(String str)    
}
