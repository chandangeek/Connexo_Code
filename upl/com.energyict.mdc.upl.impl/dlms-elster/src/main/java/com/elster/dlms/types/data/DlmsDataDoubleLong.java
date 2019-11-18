/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataDoubleLong.java $
 * Version:     
 * $Id: DlmsDataDoubleLong.java 4023 2012-02-17 13:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS double-long data type.
 *
 * @author osse
 */
public final class DlmsDataDoubleLong extends AbstractDlmsDataInteger
{
  public static final int MIN_VALUE = 0x80000000;
  public static final int MAX_VALUE = 0x7fffffff;

  public DlmsDataDoubleLong(final int value)
  {
    super(value);
  }

  @Override
  public DataType getType()
  {
    return DataType.DOUBLE_LONG;
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
