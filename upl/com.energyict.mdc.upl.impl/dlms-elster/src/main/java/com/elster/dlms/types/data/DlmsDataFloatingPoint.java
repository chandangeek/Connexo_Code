/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataFloatingPoint.java $
 * Version:     
 * $Id: DlmsDataFloatingPoint.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:24:16
 */
package com.elster.dlms.types.data;

import java.math.BigDecimal;

/**
 * This class implements the DLMS floating-point data type.
 *
 * @author osse
 */
public final class DlmsDataFloatingPoint extends AbstractDlmsDataNumber
{
  private final float value;

  public DlmsDataFloatingPoint(final float value)
  {
    super();
    this.value = value;
  }

  @Override
  public Float getValue()
  {
    return value;
  }

  @Override
  public DataType getType()
  {
    return DataType.FLOATING_POINT;
  }

  @Override
  public BigDecimal bigDecimalValue()
  {
    return new BigDecimal(Float.toString(value)); //toString is necessary
  }

}
