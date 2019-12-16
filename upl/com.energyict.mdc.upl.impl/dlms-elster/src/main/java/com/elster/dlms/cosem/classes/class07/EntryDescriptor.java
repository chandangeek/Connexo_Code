/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/EntryDescriptor.java $
 * Version:     
 * $Id: EntryDescriptor.java 4818 2012-07-11 09:01:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 3, 2011 4:27:43 PM
 */
package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataDoubleLongUnsigned;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataStructure;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry descriptor for selective access to the buffer attribute.<P>
 * See BB ed.10 p.49
 *
 * @author osse
 */
public class EntryDescriptor extends AbstractBufferAccessSelector
{
  private final long fromEntry;
  private final long toEntry;
  private final int fromSelectedValue;
  private final int toSelectedValue;

  /**
   * Constructor
   * 
   * @param fromEntry first entry to retrieve (row)
   * @param toEntry last entry to retrieve (0 for the highest possible entry)
   * @param fromSelectedValue index of first value to retrieve (channel, column)
   * @param toSelectedValue index of last value to retrieve (0 for the highest possible selected value)
   */
  public EntryDescriptor(final long fromEntry, final long toEntry, final int fromSelectedValue,
                         final int toSelectedValue)
  {
    super();
    this.fromEntry = fromEntry;
    this.toEntry = toEntry;
    this.fromSelectedValue = fromSelectedValue;
    this.toSelectedValue = toSelectedValue;
  }

  public int getId()
  {
    return 2;
  }

  /**
   * First entry to retrieve.
   *
   * @return The first entry to retrieve.
   */
  public long getFromEntry()
  {
    return fromEntry;
  }

  /**
   * Index of first value to retrieve.
   *
   * @return The index of first value to retrieve.
   */
  public int getFromSelectedValue()
  {
    return fromSelectedValue;
  }

  /**
   * Last entry to retrieve<P>
   * 0 means the highest possible entry.
   *
   * @return The last entry to retrieve.
   */
  public long getToEntry()
  {
    return toEntry;
  }

  /**
   * Index of last value to retrieve.
   * 0 means the highest possible selected value.
   *
   * @return The index of first value to retrieve.
   */
  public int getToSelectedValue()
  {
    return toSelectedValue;
  }

  public DlmsData toDlmsData()
  {
    final DlmsData[] structureElements = new DlmsData[4];
    structureElements[0] = new DlmsDataDoubleLongUnsigned(fromEntry);
    structureElements[1] = new DlmsDataDoubleLongUnsigned(toEntry);
    structureElements[2] = new DlmsDataLongUnsigned(fromSelectedValue);
    structureElements[3] = new DlmsDataLongUnsigned(toSelectedValue);
    return new DlmsDataStructure(structureElements);
  }

  @Override
  public String toString()
  {
    return "EntryDescriptor {" + "fromEntry=" + fromEntry + ", toEntry=" + toEntry + ", fromSelectedValue="
           + fromSelectedValue + ", toSelectedValue=" + toSelectedValue + '}';
  }

  @Override
  public List<CaptureObjectDefinition> filterActiveObjectDefinitions(final List<CaptureObjectDefinition> all)
  {
    if (fromSelectedValue == 0 && toSelectedValue == 0)
    {
      return new ArrayList<CaptureObjectDefinition>(all);
    }

    final ArrayList<CaptureObjectDefinition> result = new ArrayList<CaptureObjectDefinition>(all.size());

    int from = fromSelectedValue;

    if (from == 0)
    {
      from = 1;
    }



    int to=toSelectedValue;
    if (to == 0 || to> all.size() )
    {
      to = all.size();
    }

    for (int i = from-1; i <= to-1; i++)
    {
      result.add(all.get(i));
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
    final EntryDescriptor other = (EntryDescriptor)obj;
    if (this.fromEntry != other.fromEntry)
    {
      return false;
    }
    if (this.toEntry != other.toEntry)
    {
      return false;
    }
    if (this.fromSelectedValue != other.fromSelectedValue)
    {
      return false;
    }
    if (this.toSelectedValue != other.toSelectedValue)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 47 * hash + (int)(this.fromEntry ^ (this.fromEntry >>> 32));
    hash = 47 * hash + (int)(this.toEntry ^ (this.toEntry >>> 32));
    hash = 47 * hash + this.fromSelectedValue;
    hash = 47 * hash + this.toSelectedValue;
    return hash;
  }
  
  


}
