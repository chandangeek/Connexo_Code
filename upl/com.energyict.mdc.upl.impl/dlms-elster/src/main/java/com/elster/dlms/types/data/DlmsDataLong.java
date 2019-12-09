/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataLong.java $
 * Version:     
 * $Id: DlmsDataLong.java 4023 2012-02-17 13:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:29:11
 */
package com.elster.dlms.types.data;

/**
 * This class implements the DLMS long data type.
 *
 * @author osse
 */
public final class DlmsDataLong extends AbstractDlmsDataInteger
{
  public static final int MIN_VALUE = -32768;
  public static final int MAX_VALUE = 32767;

  public DlmsDataLong(final int value)
  {
    super(value);
  }

  @Override
  public DataType getType()
  {
    return DataType.LONG;
  }

  @Override
  public int getMinValue()
  {
    return MIN_VALUE;
  }

  @Override
  public int getMaxValue()
  {
    return MAX_VALUE;
  }

}
