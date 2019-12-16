/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataLong64Unsigned.java $
 * Version:     
 * $Id: DlmsDataLong64Unsigned.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:29:11
 */
package com.elster.dlms.types.data;

import com.elster.coding.CodingUtils;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This class implements the DLMS long64-unsigned data type.
 *
 * @author osse
 */
public final class DlmsDataLong64Unsigned extends AbstractDlmsDataNumber
{
  public static final BigInteger MIN_VALUE = new BigInteger(new byte[]
          {
            (byte)0x00
          });
  public static final BigInteger MAX_VALUE = new BigInteger(new byte[]
          {
            (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
            (byte)0xFF
          });
  private final BigInteger value;

  public DlmsDataLong64Unsigned(final long longValue)
  {
    super();
    if (longValue < 0)
    {
      throw new IllegalArgumentException("The value is to small");
    }
    value = BigInteger.valueOf(longValue);
  }

  public DlmsDataLong64Unsigned(final BigInteger value)
  {
    super();

    if (value.signum() == -1)
    {
      throw new IllegalArgumentException("The value is to small");
    }

    if (value.compareTo(MAX_VALUE) > 0)
    {
      throw new IllegalArgumentException("The value is to big");
    }


    this.value = value;
  }
  
      
  @Override
  public BigDecimal bigDecimalValue()
  {
    return new BigDecimal(value);
  }

  public byte[] toBytes()
  {
    final byte[] buf = value.toByteArray();

    if (buf.length >= 8)
    {
      for (int i = 0; i < buf.length - 8; i++)
      {
        if (buf[i] != 0)
        {
          throw new IllegalStateException("The number is to big");
        }
      }
      return CodingUtils.copyOfRange(buf, buf.length - 8, buf.length);
    }
    else
    {
      final byte[] result = new byte[8];
      System.arraycopy(buf, 0, result, 8 - buf.length, buf.length);
      return result;
    }
  }

  @Override
  public DataType getType()
  {
    return DataType.LONG64_UNSIGNED;
  }

  public BigInteger getMinValue()
  {
    return MIN_VALUE;
  }

  public BigInteger getMaxValue()
  {
    return MAX_VALUE;
  }

  @Override
  public BigInteger getValue()
  {
    return value;
  }

}
