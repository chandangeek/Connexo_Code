/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataTime.java $
 * Version:     
 * $Id: DlmsDataTime.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS time data type.
 *
 * @author osse
 */
public final class DlmsDataTime extends DlmsData
{
  private final DlmsTime value;

  public DlmsDataTime(final byte[] value)
  {
    super();
    this.value = new DlmsTime(value);
  }

  public DlmsDataTime(final DlmsTime value)
  {
    super();
    this.value = value;
  }

  @Override
  public DataType getType()
  {
    return DataType.TIME;
  }

  @Override
  public DlmsTime getValue()
  {
    return value;
  }

  @Override
  public String stringValue()
  {
    return value.stringValue();
  }


}
