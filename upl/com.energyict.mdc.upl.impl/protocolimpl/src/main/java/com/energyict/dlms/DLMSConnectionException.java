package com.energyict.dlms;

/**
 * <p>Title: Meter Dialup package.</p>
 * <p>Description: Modem dialup and Energy Meter protocol implementation.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: EnergyICT</p>
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */

public class DLMSConnectionException extends Exception
{
  private short sReason;

  public short getReason()
  {
     return sReason;
  }

  public DLMSConnectionException(String str)
  {
      super(str);
      this.sReason = -1;
  } // public DLMSConnectionException(String str)

  public DLMSConnectionException()
  {
      super();
      this.sReason = -1;
  } // public DLMSConnectionException()

  public DLMSConnectionException(String str, short sReason)
  {
      super(str);
      this.sReason = sReason;

  } // public DLMSConnectionException(String str)
}