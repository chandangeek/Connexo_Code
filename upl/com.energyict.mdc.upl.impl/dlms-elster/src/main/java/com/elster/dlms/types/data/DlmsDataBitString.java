/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataBitString.java $
 * Version:     
 * $Id: DlmsDataBitString.java 4795 2012-07-06 14:51:05Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.data.DlmsData.DataType;

/**
 * This class implements the DLMS bit-string data type.
 * <P>
 * The "value" is of the type {@link BitString}.
 *
 * @author osse
 */
public final class DlmsDataBitString extends DlmsData
{
  private final BitString value;
  public static final Class<?> VALUE_TYPE = BitString.class;

  public DlmsDataBitString(final BitString value)
  {
    super();
    this.value = value;
  }

  @Override
  public DataType getType()
  {
    return DataType.BIT_STRING;
  }

  @Override
  public BitString getValue()
  {
    return value;
  }

  @Override
  public String stringValue()
  {
    return value.toString(false, 1<<16);
  }
  
  

}
