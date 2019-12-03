/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/data/DlmsDataCollection.java $
 * Version:     
 * $Id: DlmsDataCollection.java 4385 2012-04-19 14:36:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 12:21:46
 */
package com.elster.dlms.types.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is the base class for DLMS data collections like array and structure.
 * <P>
 * The "items" is an array of DlmsData.<P>
 * This class implements the List interface by itself and delegates all methods to the "items".
 *
 *
 * @author osse
 */
public abstract class DlmsDataCollection extends DlmsData implements Iterable<DlmsData>
{
  protected final DlmsData[] items;

  protected static DlmsData[] convert(final IDlmsDataProvider[] values)
  {
    final DlmsData[] dataArray = new DlmsData[values.length];

    for (int i = 0; i < values.length; i++)
    {
      dataArray[i] = values[i].toDlmsData();
    }
    return dataArray;
  }

  public static final DlmsData[] EMPTY_DLMS_DATA_ARRAY = new DlmsData[0];

  DlmsDataCollection(final DlmsData[] values)
  {
    super();
    items = values.clone();
  }

  DlmsDataCollection(final Collection<? extends DlmsData> values)
  {
    super();
    items = values.toArray(EMPTY_DLMS_DATA_ARRAY);
  }

  DlmsDataCollection(final DlmsData first, final DlmsData... more)
  {
    super();
    items = new DlmsData[1 + more.length];
    items[0] = first;
    System.arraycopy(more, 0, items, 1, more.length);
  }

  @Override
  public String toString()
  {
    return toString("");
  }

  protected static final String EOL = "\r\n";

  @Override
  public String toString(final String prefix)
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(getType().getOrgName()).append("(").append(size()).append(" elements)=" + EOL);
    sb.append(prefix).append("{" + EOL);
    for (DlmsData d : items)
    {
      sb.append(d.toString(prefix + "\t")).append(EOL);
    }
    sb.append(prefix).append("}");
    return sb.toString();
  }

  /**
   * Single line string.<P>
   *
   * @return
   */
  @Override
  public String toSingleLineString(final int maxElementsPerCollection)
  {
    int elementsToShow = (maxElementsPerCollection >= 0) ? maxElementsPerCollection : size();
    elementsToShow = Math.min(size(), elementsToShow);


    if (elementsToShow == 0)
    {
      return getType().getOrgName() + " (" + size() + " elements)";
    }
    else
    {
      final StringBuilder sb = new StringBuilder();
      sb.append(getType().getOrgName()).append("(").append(size()).append(" elements)={");

      for (int i = 0; i < elementsToShow; i++)
      {
        if (i > 0)
        {
          sb.append(", ");
        }
        sb.append(items[i].toSingleLineString(maxElementsPerCollection));
      }

      if (elementsToShow < size())
      {
        sb.append(", <").append(size() - elementsToShow).append(" more elements>");
      }

      sb.append("}");
      return sb.toString();
    }

  }

  public int size()
  {
    return items.length;
  }

  public boolean isEmpty()
  {
    return items.length == 0;
  }

  //@Override
  public Iterator<DlmsData> iterator()
  {
    return Arrays.asList(items).iterator();
  }

  protected abstract String stringValueStartChar();

  protected abstract String stringValueEndChar();

  @Override
  public String stringValue()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(stringValueStartChar());
    for (int i = 0; i < items.length; i++)
    {
      if (i > 0)
      {
        sb.append(",");
      }
      sb.append(items[i].stringValue());
    }
    sb.append(stringValueEndChar());
    return sb.toString();
  }

  public DlmsData get(final int index)
  {
    return items[index];
  }

  @Override
  public boolean equals(final Object obj)
  {

    if (this == obj) //shortcut
    {
      return true;
    }

    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }

    final DlmsDataCollection other = (DlmsDataCollection)obj;
    if (getType() != other.getType())
    {
      return false;
    }

    if (size() != other.size())
    {
      return false;
    }


    for (int i = 0; i < size(); i++)
    {
      if (!get(i).equals(other.get(i)))
      {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + getType().hashCode();

    for (int i = 0; i < size(); i++)
    {
      hash = 37 * hash + get(i).hashCode();
    }
    return hash;
  }

  @Override
  public DlmsData[] getValue()
  {
    return items.clone();
  }

}
