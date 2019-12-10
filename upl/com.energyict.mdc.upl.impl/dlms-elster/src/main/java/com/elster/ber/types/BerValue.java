/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValue.java $
 * Version:     
 * $Id: BerValue.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 11:48:39
 */
package com.elster.ber.types;

/**
 * Base class for values of BER.<B>
 * Wraps a value from a given type and combines it with their {@link BerId }.
 *
 * @author osse
 */
public abstract class BerValue
{
  private final BerId identifier;

  /**
   * Constructor with id only.
   *
   * @param identifier The id.
   */
  protected BerValue(BerId identifier)
  {
    this.identifier = identifier;
  }

  /**
   * Returns the id for this value.
   * @return The id.
   */
  public BerId getIdentifier()
  {
    return identifier;
  }

  /**
   * The wrapped value.
   *
   * @return The value
   */
  public abstract Object getValue();

//   /**
//   * Sets the wrapped value.
//   */
//  public void setValue(T value)
//  {
//    this.value = value;
//  }
  @Override
  public String toString()
  {
    BerDescriber describer = new BerDescriber();
    describe(describer);
    return describer.toString();
  }

  /**
   * Describes the value.
   * <P>
   * It writes the class name, the identifier to the describer and calls
   * {@link #describeValue(com.elster.ber.types.BerDescriber)} for describing the
   * wrapped value.
   *
   * @param describer The description will be written to this describer.
   */
  protected void describe(BerDescriber describer)
  {
    describer.writeLn(getClass().getSimpleName());
    describer.writeLn(identifier.toString());
    describeValue(describer);
  }

  /**
   * Called by {@link #describe(com.elster.ber.types.BerDescriber)} to describe the
   * wrapped value.
   *
   *
   * @param describer
   */
  protected void describeValue(BerDescriber describer)
  {
    describer.writeLn(getValue().toString());
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
    final BerValue other =
            (BerValue)obj;
    if (this.identifier != other.identifier && (this.identifier == null || !this.identifier.equals(
            other.identifier)))
    {
      return false;
    }
    if (this.getValue() != other.getValue() && (this.getValue() == null || !this.getValue().equals(other.
            getValue())))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 89 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
    hash = 89 * hash + (this.getValue() != null ? this.getValue().hashCode() : 0);
    return hash;
  }

}
