/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataDate.java $
 * Version:     
 * $Id: DlmsDataDate.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS date data type.
 * <P>
 * (acc. Blue Book ed.9 p.23)
 * @author osse
 */
public final class DlmsDataDate extends DlmsData
{
  private final DlmsDate value;

  public DlmsDataDate(final byte[] value)
  {
    super();
    this.value = new DlmsDate(value);
  }

  public DlmsDataDate(final DlmsDate newValue)
  {
    super();
    this.value = newValue;
  }

  @Override
  public DataType getType()
  {
    return DataType.DATE;
  }

  @Override
  public DlmsDate getValue()
  {
    return value;
  }

  @Override
  public String stringValue()
  {
    return value.stringValue();
  }
  
  

}
