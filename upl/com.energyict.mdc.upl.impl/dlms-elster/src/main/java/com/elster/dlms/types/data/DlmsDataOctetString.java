/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataOctetString.java $
 * Version:     
 * $Id: DlmsDataOctetString.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 13:25:34
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.coding.CodingUtils;
import java.util.Arrays;

/**
 * This class implements the DLMS octet-string data type.
 *
 * @author osse
 */
public final class DlmsDataOctetString extends DlmsData
{
  private final byte[] value;

  public DlmsDataOctetString(final byte[] value)
  {
    super();
    this.value = value.clone();
  }

  @Override
  public DataType getType()
  {
    return DataType.OCTET_STRING;
  }

  @Override
  public String stringValue()
  {
    return CodingUtils.byteArrayToString(value);
  }

  @Override
  public String toString()
  {
    return getType().getOrgName() + "=" + CodingUtils.byteArrayToString(value);
  }

  /**
   * Returns the length of the octet string.
   *
   * @return The length of the octet string.
   */
  public int size()
  {
    return value.length;
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
    final DlmsDataOctetString other = (DlmsDataOctetString)obj;
    if (!Arrays.equals(this.value, other.value))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 53 * hash + Arrays.hashCode(this.value);
    return hash;
  }

  @Override
  public byte[] getValue()
  {
    return value.clone();
  }

}
