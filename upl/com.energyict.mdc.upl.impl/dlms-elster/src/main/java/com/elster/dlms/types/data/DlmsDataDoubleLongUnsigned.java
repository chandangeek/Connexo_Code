/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataDoubleLongUnsigned.java $
 * Version:     
 * $Id: DlmsDataDoubleLongUnsigned.java 4023 2012-02-17 13:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS double-long-unsigned data type.
 *
 * @author osse
 */
public final class DlmsDataDoubleLongUnsigned extends AbstractDlmsDataLong
{
  public static final long MIN_VALUE = 0;
  public static final long MAX_VALUE = 0xFFFFFFFFL;

  public DlmsDataDoubleLongUnsigned(final long value)
  {
    super(value);
  }

  @Override
  public DataType getType()
  {
    return DataType.DOUBLE_LONG_UNSIGNED;
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
