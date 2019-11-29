/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/AbstractDlmsDataInteger.java $
 * Version:     
 * $Id: AbstractDlmsDataInteger.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import java.math.BigDecimal;

/**
 * This class is the base class for DLMS integer (max. 32 bit) data types.
 * <P>
 * The data will be stored as {@code integer}.
 * <P>
 * <b>Do not use this class directly. This class maybe deleted in future versions</b>
 *
 * @author osse
 */
public abstract class AbstractDlmsDataInteger extends AbstractDlmsDataNumber
{
  protected final int value;
  public static final Class<?> VALUE_TYPE = Integer.class;

  //package private for DlmsDataEnum
  static void rangeCheck(final int value, final int min, final int max)
  {
    if (value < min || value > max)
    {
      throw new IllegalArgumentException("Value out of range. Value:" + value + " Min:" + min
                                         + " Max:" + max);
    }
  }

  //Only subtypes in package allowed
  AbstractDlmsDataInteger(final int value)
  {
    super();
    if (!checkRange(value))
    {
      rangeCheck(value, getMinValue(), getMaxValue());
    }
    this.value = value;
  }

  @Override
  public Integer getValue()
  {
    return value;
  }

  @Override
  public BigDecimal bigDecimalValue()
  {
    return BigDecimal.valueOf(value);
  }

  public final boolean checkRange(final int value)
  {
    return value >= getMinValue() && value <= getMaxValue();
  }

  public abstract int getMinValue();

  public abstract int getMaxValue();

}
