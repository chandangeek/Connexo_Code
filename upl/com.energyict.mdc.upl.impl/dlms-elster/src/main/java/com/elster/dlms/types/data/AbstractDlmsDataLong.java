/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/AbstractDlmsDataLong.java $
 * Version:     
 * $Id: AbstractDlmsDataLong.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import java.math.BigDecimal;

/**
 * This class is the base class for DLMS integer (max. 64 bit) data types.
 * <P>
 * The data will be stored as {@code long}.
 * <P>
 * <b>Do not use this class directly. This class maybe deleted in future versions</b>
 *
 * @author osse
 */
public abstract class AbstractDlmsDataLong extends AbstractDlmsDataNumber
{
  public final static Class<?> VALUE_TYPE = Long.class;
  protected final long value;

  //Only subtypes in package allowed
  AbstractDlmsDataLong(final long value)
  {
    super();
    if (!checkRange(value))
    {
      throw new IllegalArgumentException("Value out of range. Value:" + value + " Min:" + getMinValue()
                                         + " Max:" + getMaxValue());
    }
    this.value = value;
  }

  @Override
  public Long getValue()
  {
    return value;
  }
  
  @Override
  public BigDecimal bigDecimalValue()
  {
    return BigDecimal.valueOf(value);
  }

  public final boolean checkRange(final long value)
  {
    return value >= getMinValue() && value <= getMaxValue();
  }

  public abstract long getMinValue();

  public abstract long getMaxValue();

}
