/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/obis/DlmsDataTypeSet.java $
 * Version:     
 * $Id: DlmsDataTypeSet.java 4495 2012-05-11 12:39:19Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 2, 2012 1:49:09 PM
 */
package com.elster.obis;

import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import java.util.*;

/**
 * Immutable set of DlmsData.DataType with one helper method.
 *
 * @author osse
 */
public class DlmsDataTypeSet implements Set<DlmsData.DataType>
{
  private final Set<DlmsData.DataType> types;

  public DlmsDataTypeSet(final Collection<DataType> types)
  {
    this.types = EnumSet.copyOf(types);
  }

  public boolean hasNumberTypesOnly()
  {
    boolean result = true;
    for (DataType d : types)
    {
      if (!d.isNumberType())
      {
        result = false;
        break;
      }
    }
    return result;
  }

  public String toString()
  {
    return types.toString();
  }

  public <T> T[] toArray(final T[] a)
  {
    return types.toArray(a);
  }

  public Object[] toArray()
  {
    return types.toArray();
  }

  public int size()
  {
    return types.size();
  }

  public boolean retainAll(final Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(final Collection<?> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean remove(final Object o)
  {
    throw new UnsupportedOperationException();
  }

  public Iterator<DataType> iterator()
  {
    return Collections.unmodifiableSet(types).iterator();
  }

  public boolean isEmpty()
  {
    return types.isEmpty();
  }

  public int hashCode()
  {
    return types.hashCode();
  }

  public boolean equals(final Object o)
  {
    return types.equals(o);
  }

  public boolean containsAll(final Collection<?> c)
  {
    return types.containsAll(c);
  }

  public boolean contains(final Object o)
  {
    return types.contains(o);
  }

  public void clear()
  {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(final Collection<? extends DataType> c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean add(final DataType e)
  {
    throw new UnsupportedOperationException();
  }

}
