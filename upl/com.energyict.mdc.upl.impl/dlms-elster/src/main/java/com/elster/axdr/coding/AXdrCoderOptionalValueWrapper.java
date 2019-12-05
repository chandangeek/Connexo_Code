/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderOptionalValueWrapper.java $
 * Version:     
 * $Id: AXdrCoderOptionalValueWrapper.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:37:48
 */
package com.elster.axdr.coding;

import com.elster.coding.AbstractCoder;
import java.io.IOException;

/**
 * This class wraps another encoder as an optional part.<P>
 *
 * @author osse
 */
public class AXdrCoderOptionalValueWrapper<T> extends AbstractAXdrCoder<T>
{
  private final AbstractCoder<T> wrapped;

  /**
   * Constructor of the "optional" wrapper.<P>
   *
   * @param wrapped The optional part of a PDU (protocol data unit)
   */
  public AXdrCoderOptionalValueWrapper(AbstractCoder<T> wrapped)
  {
    this.wrapped = wrapped;
  }

  /**
   * Return the wrapped coder.
   *
   * @return The wrapped coder.
   */
  public AbstractCoder<T> getWrapped()
  {
    return wrapped;
  }


  /**
   * Encodes the object.<P>
   * If the object is <b>null</b> the optional part will be not used. (0 will be written to the stream).
   *
   * @param object The object to encode or <b>null</b> if the optional part is not used.
   * @param out The output stream to encode the object to.
   * @throws IOException
   */
  @Override
  public void encodeObject(T object, AXdrOutputStream out) throws IOException
  {
    boolean activated = object != null;
    out.writeBoolean(activated);
    if (activated)
    {
      wrapped.encodeObject(object, out);
    }
  }

  /**
   * Reads and decodes an object from the input stream.<P>
   * If the optional part is not used <b>null</b>  will be returned.
   *
   * @param in The input stream
   * @return The decoded object or <b>null</b>
   * @throws IOException
   */
  @Override
  public T decodeObject(AXdrInputStream in) throws IOException
  {
    boolean activated = in.readBoolean();
    return activated ? wrapped.decodeObject(in) : null;
  }

}
