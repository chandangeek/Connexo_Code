/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/RangeDescriptor.java $
 * Version:     
 * $Id: RangeDescriptor.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 27, 2011 11:27:22 AM
 */
package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataNull;
import com.elster.dlms.types.data.DlmsDataStructure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Range descriptor for selective access.<P>
 * See BB ed.10 p.48
 *
 * @author osse
 */
public class RangeDescriptor extends AbstractBufferAccessSelector
{
  private final CaptureObjectDefinition restrictingObject;
  private final DlmsData fromValue;
  private final DlmsData toValue;
  private final CaptureObjectDefinition[] selectedValues;

  public RangeDescriptor(final CaptureObjectDefinition restrictingObject,final DlmsData fromValue,final DlmsData toValue,
                         final CaptureObjectDefinition[] selectedValues)
  {
    super();
    this.restrictingObject = restrictingObject;
    this.fromValue = fromValue;
    this.toValue = toValue;

    if (selectedValues == null)
    {
      this.selectedValues = new CaptureObjectDefinition[0];
    }
    else
    {
      this.selectedValues = selectedValues.clone();
    }
  }

  public DlmsData getFromValue()
  {
    return fromValue;
  }

  public CaptureObjectDefinition getRestrictingObject()
  {
    return restrictingObject;
  }

  public CaptureObjectDefinition[] getSelectedValues()
  {
    return selectedValues.clone();
  }

  public DlmsData getToValue()
  {
    return toValue;
  }

  //@Override
  public int getId()
  {
    return 1;
  }

  //@Override
  public DlmsData toDlmsData()
  {
    DlmsData[] structureElements = new DlmsData[4];
    structureElements[0] = restrictingObject.toDlmsData();
    if (fromValue == null)
    {
      structureElements[1] = new DlmsDataNull();
    }
    else
    {
      structureElements[1] = fromValue;
    }

    if (toValue == null)
    {
      structureElements[2] = new DlmsDataNull();
    }
    else
    {
      structureElements[2] = toValue;
    }

    DlmsData[] selectedValuesElements = new DlmsData[selectedValues.length];
    for (int i = 0; i < selectedValuesElements.length; i++)
    {
      selectedValuesElements[i] = selectedValues[i].toDlmsData();
    }

    structureElements[3] = new DlmsDataArray(selectedValuesElements);
    return new DlmsDataStructure(structureElements);
  }

  @Override
  public String toString()
  {
    return restrictingObject.toString() + " " + fromValue + "-" + toValue + ", " + selectedValues.length
           + " values";
  }

  @Override
  public List<CaptureObjectDefinition> filterActiveObjectDefinitions(final List<CaptureObjectDefinition> all)
  {
    if (selectedValues == null || selectedValues.length == 0)
    {
      return new ArrayList<CaptureObjectDefinition>(all);
    }

    final ArrayList<CaptureObjectDefinition> result = new ArrayList<CaptureObjectDefinition>(all.size());

    for (CaptureObjectDefinition candidate : all)
    {
      for (CaptureObjectDefinition s : selectedValues)
      {
        if (candidate.equals(s))
        {
          result.add(s);
          break;
        }
      }
    }
    return result;
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
    final RangeDescriptor other = (RangeDescriptor)obj;
    if (this.restrictingObject != other.restrictingObject && (this.restrictingObject == null || !this.restrictingObject.
            equals(other.restrictingObject)))
    {
      return false;
    }
    if (this.fromValue != other.fromValue && (this.fromValue == null
                                              || !this.fromValue.equals(other.fromValue)))
    {
      return false;
    }
    if (this.toValue != other.toValue && (this.toValue == null || !this.toValue.equals(other.toValue)))
    {
      return false;
    }
    if (!Arrays.deepEquals(this.selectedValues, other.selectedValues))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 79 * hash + (this.restrictingObject != null ? this.restrictingObject.hashCode() : 0);
    hash = 79 * hash + (this.fromValue != null ? this.fromValue.hashCode() : 0);
    hash = 79 * hash + (this.toValue != null ? this.toValue.hashCode() : 0);
    hash = 79 * hash + Arrays.deepHashCode(this.selectedValues);
    return hash;
  }

}
