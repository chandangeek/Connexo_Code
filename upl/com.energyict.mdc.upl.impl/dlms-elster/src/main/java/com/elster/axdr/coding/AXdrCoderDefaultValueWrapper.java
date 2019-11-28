/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderDefaultValueWrapper.java $
 * Version:     
 * $Id: AXdrCoderDefaultValueWrapper.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:37:48
 */
package com.elster.axdr.coding;

import com.elster.coding.AbstractCoder;
import java.io.IOException;

/**
 * This class wraps another encoder as an default value.<P>
 * If the default value equals the value that should be encoded, the 0 byte will be written to
 * indicate that the default value is used.<P>
 * If the 0 byte was decoded, the default value will be returned.
 *
 * @author osse
 */
public class AXdrCoderDefaultValueWrapper<T> extends AbstractAXdrCoder<T>
{
  private final AbstractCoder<T> wrapped;
  private final T defaultValue;

  /**
   * Creates the de-/encoder.
   *
   * @param wrapped The wrapped de-/encoder
   * @param defaultValue The default value. This value <b>must</b> implement the a working equals method.
   */
  public AXdrCoderDefaultValueWrapper(AbstractCoder<T> wrapped, T defaultValue)
  {
    this.wrapped = wrapped;
    this.defaultValue = defaultValue;
  }

  public AbstractCoder<T> getWrapped()
  {
    return wrapped;
  }

  public T getDefaultValue()
  {
    return defaultValue;
  }

  @Override
  public void encodeObject(T object, AXdrOutputStream out) throws IOException
  {
    if (object == null || defaultValue.equals(object))
    {
      out.writeBoolean(false);
    }
    else
    {
      out.writeBoolean(true);
      wrapped.encodeObject(object, out);
    }

  }

  @Override
  public T decodeObject(AXdrInputStream in) throws IOException
  {
    boolean activated = in.readBoolean();
    if (activated)
    {
      return wrapped.decodeObject(in);
    }
    else
    {
      return defaultValue;
    }
  }

}
