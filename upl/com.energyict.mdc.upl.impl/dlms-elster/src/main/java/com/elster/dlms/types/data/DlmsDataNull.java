/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataNull.java $
 * Version:     
 * $Id: DlmsDataNull.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:17:48
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS data null type.
 *
 * @author osse
 */
public final class DlmsDataNull extends DlmsData
{
  @Override
  public DataType getType()
  {
    return DataType.NULL_DATA;
  }

  /**
   * Returns always {@code null}
   *
   * @return Always {@code null}
   */
  @Override
  public Object getValue()
  {
    return null;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }

    final DlmsData other = (DlmsData)obj;
    if (getType() != other.getType())
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    return 11625432;
  }

  @Override
  public String stringValue()
  {
    return "";
  }
  
  

}
