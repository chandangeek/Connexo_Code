/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataDateTime.java $
 * Version:     
 * $Id: DlmsDataDateTime.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS date-time data type.
 * <P>
 * (acc. Blue Book ed.9 p.23)
 * @author osse
 */
public final class DlmsDataDateTime extends DlmsData
{
  private final DlmsDateTime value;

  public DlmsDataDateTime(final byte[] value)
  {
    super();
    this.value = new DlmsDateTime(value);
  }

  public DlmsDataDateTime(final DlmsDateTime newValue)
  {
    super();
    this.value = newValue;
  }

  @Override
  public DataType getType()
  {
    return DataType.DATE_TIME;
  }

  @Override
  public DlmsDateTime getValue()
  {
    return value;
  }
  
  @Override
  public String stringValue()
  {
    return value.stringValue();
  }

}
