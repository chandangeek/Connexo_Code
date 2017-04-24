/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

public class HDLCConnectionException extends Exception
{
  private short sReason;

  public short getReason()
  {
     return sReason;
  }

  public HDLCConnectionException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public HDLCConnectionException(String str)

  public HDLCConnectionException()
  {
      super();
      this.sReason = -1;
  } // public HDLCConnectionException()

  public HDLCConnectionException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public HDLCConnectionException(String str)
}