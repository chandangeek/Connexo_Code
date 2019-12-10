/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataBoolean.java $
 * Version:     
 * $Id: DlmsDataBoolean.java 4024 2012-02-17 13:48:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS boolean data type.
 *
 * @author osse
 */
public final class DlmsDataBoolean extends DlmsData
{
  public final static Class<?> VALUE_TYPE = Boolean.class;
  private final boolean value;

  public DlmsDataBoolean(final boolean value)
  {
    super();
    this.value = value;
  }

  @Override
  public DataType getType()
  {
    return DataType.BOOLEAN;
  }

  @Override
  public Boolean getValue()
  {
    return value;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final DlmsDataBoolean other = (DlmsDataBoolean)obj;
    if (this.value != other.value)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    if (value)
    {
      return 0x0646d8a1;
    }
    else
    {
      return 0x7b89a570;
    }
  }

}
