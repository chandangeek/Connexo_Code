/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/ber/types/BerValueBase.java $
 * Version:     
 * $Id: BerValueBase.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Feb 15, 2012 1:36:16 PM
 */
package com.elster.ber.types;

/**
 * Base class for BER values of a specific type
 *
 * @author osse
 */
public abstract class BerValueBase<T> extends BerValue
{
  protected final T value;

  /**
   * Constructor with id and value.
   *
   * @param identifier The identifier
   * @param value The value
   */
  public BerValueBase(final BerId identifier, final T value)
  {
    super(identifier);
    this.value = value;
  }

  @Override
  public T getValue()
  {
    return value;
  }

}
