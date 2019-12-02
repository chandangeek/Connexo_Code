/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataLong64.java $
 * Version:     
 * $Id: DlmsDataLong64.java 4023 2012-02-17 13:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:29:11
 */
package com.elster.dlms.types.data;

/**
 * This class implements the DLMS long64 data type.
 *
 * @author osse
 */
public final class DlmsDataLong64 extends AbstractDlmsDataLong
{
  public static final long MIN_VALUE = 0x8000000000000000L;
  public static final long MAX_VALUE = 0x7fffffffffffffffL;

  public DlmsDataLong64(final long value)
  {
    super(value);
  }

  @Override
  public DataType getType()
  {
    return DataType.LONG64;
  }

  @Override
  public long getMinValue()
  {
    return MIN_VALUE;
  }

  @Override
  public long getMaxValue()
  {
    return MAX_VALUE;
  }

}
