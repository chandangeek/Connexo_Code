/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleDataObject.java $
 * Version:     
 * $Id: SimpleDataObject.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:55:47 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataBitString;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import java.io.IOException;

/**
 * Class for Objects of COSEM class "Data".
 *
 * @author osse
 */
public class SimpleDataObject extends SimpleCosemObject
{
  public SimpleDataObject(SimpleCosemObjectDefinition definition, SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public DlmsData getValue() throws IOException
  {
    return executeGet(2, DlmsData.class, true);
  }

  public DlmsData getValue(final boolean forceRead) throws IOException
  {
    return executeGet(2, DlmsData.class, forceRead);
  }

  /**
   * checks if the value is of numerical type
   * 
   * @return true if value is a number
   */
  public boolean isNumber() throws IOException
  {
    return (getValue(false).getValue() instanceof Number);
  }

  public Number getValueAsNumber() throws IOException
  {
    return (Number)(getValue().getValue());
  }

  /**
   * converts value to a string
   * @return value as a string, 
   *         null if value is not of type "number", "octet string", "visible string", "bit string".
   * 
   * A BitString will be returned as an list of integers of the active bits numbers. The bit numbers are one based.
   * The list items will be separated by dots (".").  If no bits are set "0" will be returned.
   *       
   */
  public String getValueAsString() throws IOException
  {
    final DlmsData value = getValue();

    if (isNumber())
    {
      return value.getValue().toString();
    }

    if (value.getType() == DlmsData.DataType.OCTET_STRING)
    {
      return new String(((DlmsDataOctetString)value).getValue());
    }

    if (value.getType() == DlmsData.DataType.VISIBLE_STRING)
    {
      return ((DlmsDataVisibleString)value).getValue();
    }

    if (value.getType() == DlmsData.DataType.BIT_STRING)
    {
      final BitString bitString = ((DlmsDataBitString)value).getValue();
      final int[] activeBits = bitString.getActiveBits();

      if (activeBits.length == 0)
      {
        return "0";
      }
      else
      {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activeBits.length; i++)
        {
          if (i > 0)
          {
            sb.append('.');
          }
          sb.append(activeBits[i] + 1);
        }
        return sb.toString();
      }
    }
    
    return value.stringValue();
  }

  public void setValue(final DlmsData value) throws IOException
  {
    getManager().executeSetData(getDefinition(), 2, value);
  }

  public void setStringValue(final String strval) throws IOException
  {
    setValue(new DlmsDataVisibleString(strval));
  }

}
