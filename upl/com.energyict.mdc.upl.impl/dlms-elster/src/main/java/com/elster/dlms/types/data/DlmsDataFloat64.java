/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataFloat64.java $
 * Version:     
 * $Id: DlmsDataFloat64.java 6737 2013-06-12 07:16:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:24:16
 */
package com.elster.dlms.types.data;

import java.math.BigDecimal;

/**
 * This class implements the DLMS float64 data type.
 *
 * @author osse
 */
public final class DlmsDataFloat64 extends AbstractDlmsDataNumber
{
  private final double value;

  public DlmsDataFloat64(final double value)
  {
    super();
    this.value = value;
  }

  @Override
  public Double getValue()
  {
    return value;
  }

  @Override
  public DataType getType()
  {
    return DataType.FLOAT64;
  }

  @Override
  public BigDecimal bigDecimalValue()
  {
    return BigDecimal.valueOf(value);
  }
  
}
