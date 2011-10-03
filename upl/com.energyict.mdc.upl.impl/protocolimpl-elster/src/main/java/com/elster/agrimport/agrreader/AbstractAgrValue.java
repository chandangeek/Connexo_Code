/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AbstractAgrValue.java $
 * Version:     
 * $Id: AbstractAgrValue.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  23.07.2009 09:33:07
 */
package com.elster.agrimport.agrreader;

/**
 * This class is an abstract base class for agr values.
 *
 * @author osse
 */
public abstract class AbstractAgrValue<T> implements IAgrValue<T>
{
  private T value;

  public AbstractAgrValue()
  {
  }

  public AbstractAgrValue(T value)
  {
    this.value = value;
  }

  public T getValue()
  {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(final T value)
  {
    this.value = value;
  }

  @Override
  public String toString()
  {
    if (getValue() == null)
    {
      return getClass().getSimpleName() + "()";
    }
    else
    {
      return getClass().getSimpleName() + "(" + getValue().toString() + ")";
    }
  }

  @Override
  @SuppressWarnings("unchecked")
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
    final AbstractAgrValue<T> other =
            (AbstractAgrValue<T>)obj;
      return !((this.value != other.value) && (this.value == null || !this.value.equals(other.value)));
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 41 * hash + (this.value != null ? this.value.hashCode() : 0);
    return hash;
  }

}
