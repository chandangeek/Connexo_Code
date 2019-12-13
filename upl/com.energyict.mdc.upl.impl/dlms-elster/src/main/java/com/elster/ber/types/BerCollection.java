/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerCollection.java $
 * Version:     
 * $Id: BerCollection.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 12:04:23
 */
package com.elster.ber.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Collection for {@code BerValue}'s.<P> It provides special methods for finding values.
 *
 * @author osse
 */
public class BerCollection extends BerValueBase<List<BerValue>> implements List<BerValue>
{
  public BerCollection(final BerId identifier,
                       final List<BerValue> value)
  {
    super(identifier, new ArrayList<BerValue>(value));
  }

  public BerCollection(final BerId identifier)
  {
    super(identifier, new ArrayList<BerValue>());
  }

  @Override
  protected void describeValue(BerDescriber describer)
  {
    describer.writeLn("{");
    describer.incLevel();
    boolean first = true;

    for (BerValue v : this)
    {
      if (first)
      {
        first = false;
      }
      else
      {
        describer.writeLn("----");
      }
      v.describe(describer);
    }
    describer.decLevel();
    describer.writeLn("}");
  }

  @Override
  public List<BerValue> getValue()
  {
    return value;
  }

  /**
   * Searches a BerValue below this collection.<P> All id's except the last must lead to an underlying {@link BerCollection
   * }
   * otherwise {@code null} will be returned.<P> The last id must lead to the {@code berValueClass}.<P>
   *
   * @param <T> The type of the BER value returned.
   * @param berValueClass The class of expected object.
   * @param ids The way to the expected object.
   * @return The {@code BerValue} or null if the {@code BerValue} does not exists or if the {@code BerValue}
   * has the wrong type.
   */
  public <T extends BerValue> T findBerValue(Class<T> berValueClass, BerId... ids)
  {
    BerCollection current = this;

    for (int i = 0; i < ids.length - 1; i++)
    {
      current = current.findBerValue(BerCollection.class, ids[i]);
      if (current == null)
      {
        return null;
      }
    }

    for (BerValue v : current)
    {
      if (v.getIdentifier().equals(ids[ids.length - 1]))
      {
        if (berValueClass.isAssignableFrom(v.getClass()))
        {
          return berValueClass.cast(v);
          //return (T) v;
        }
        else
        {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Exactly like {@link #findBerValue(java.lang.Class, com.elster.ber.types.BerId[])} with the difference
   * that the value (see {@link BerValue#getValue() }) of the resulting BER value will be returned.
   *
   */
  public <T> T findValue(Class<T> valueClass, BerId... ids)
  {
    BerCollection current = this;

    for (int i = 0; i < ids.length - 1; i++)
    {
      current = current.findBerValue(BerCollection.class, ids[i]);
      if (current == null)
      {
        return null;
      }
    }

    for (BerValue v : current)
    {
      if (v.getIdentifier().equals(ids[ids.length - 1]))
      {
        if (valueClass.isAssignableFrom(v.getValue().getClass()))
        {
          return valueClass.cast(v.getValue());
        }
        else
        {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Checks if an value specified by the id's exists.
   *
   * @param ids The path to the value.
   * @return {@code true} if an value exists.
   */
  public boolean valueExists(BerId... ids)
  {
    BerValue valueFound = findBerValue(BerValue.class, ids);
    return valueFound != null;
  }

  /**
   * Checks if an value specified by the id's exists.
   *
   * @param ids The path to the value.
   * @return {@code true} if an value exists.
   */
  public boolean valueExists(Class<?> valueType, BerId... ids)
  {
    BerValue valueFound = findBerValue(BerValue.class, ids);
    return valueFound != null && valueType.isAssignableFrom(valueFound.getValue().getClass());
  }

  // <editor-fold defaultstate="collapsed" desc="delegated methods">
  public int size()
  {
    return getValue().size();
  }

  public boolean isEmpty()
  {
    return getValue().isEmpty();
  }

  public boolean contains(Object o)
  {
    return getValue().isEmpty();
  }

  public Iterator<BerValue> iterator()
  {
    return getValue().iterator();
  }

  public boolean add(BerValue e)
  {
    return getValue().add(e);
  }

  public boolean remove(Object o)
  {
    return getValue().remove(o);
  }

  public boolean containsAll(Collection<?> c)
  {
    return getValue().containsAll(c);
  }

  public boolean addAll(Collection<? extends BerValue> c)
  {
    return getValue().addAll(c);
  }

  public boolean addAll(int index,
                        Collection<? extends BerValue> c)
  {
    return getValue().addAll(index, c);
  }

  public boolean removeAll(Collection<?> c)
  {
    return getValue().removeAll(c);
  }

  public boolean retainAll(Collection<?> c)
  {
    return getValue().retainAll(c);
  }

  public void clear()
  {
    getValue().clear();
  }

  public BerValue get(int index)
  {
    return getValue().get(index);
  }

  public BerValue set(int index, BerValue element)
  {
    return getValue().set(index, element);
  }

  public void add(int index, BerValue element)
  {
    getValue().add(index, element);
  }

  public BerValue remove(int index)
  {
    return getValue().remove(index);
  }

  public int indexOf(Object o)
  {
    return getValue().indexOf(o);
  }

  public int lastIndexOf(Object o)
  {
    return getValue().lastIndexOf(o);
  }

  public ListIterator<BerValue> listIterator()
  {
    return getValue().listIterator();
  }

  public ListIterator<BerValue> listIterator(int index)
  {
    return getValue().listIterator(index);
  }

  public List<BerValue> subList(int fromIndex, int toIndex)
  {
    return getValue().subList(fromIndex, toIndex);
  }

  public Object[] toArray()
  {
    return getValue().toArray();
  }

  public <T> T[] toArray(T[] a)
  {
    return getValue().toArray(a);
  }// </editor-fold>

}
