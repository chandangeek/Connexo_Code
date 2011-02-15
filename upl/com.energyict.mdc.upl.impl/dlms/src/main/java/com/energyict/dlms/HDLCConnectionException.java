package com.energyict.dlms;

/**
 * <p>Title: Meter Dialup package.</p>
 * <p>Description: Modem dialup and Energy Meter protocol implementation.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: EnergyICT</p>
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */

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