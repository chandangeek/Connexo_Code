/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataVisibleString.java $
 * Version:     
 * $Id: DlmsDataVisibleString.java 4023 2012-02-17 13:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:25:34
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS visible-string data type.
 *
 * @author osse
 */
public final class DlmsDataVisibleString extends DlmsData
{
  private final String value;

  public DlmsDataVisibleString(final String value)
  {
    super();
    this.value = value;
  }

  @Override
  public DataType getType()
  {
    return DataType.VISIBLE_STRING;
  }

  @Override
  public String getValue()
  {
    return value;
  }

}
