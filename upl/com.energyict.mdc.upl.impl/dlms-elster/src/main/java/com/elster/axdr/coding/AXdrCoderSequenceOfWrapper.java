/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AXdrCoderSequenceOfWrapper.java $
 * Version:     
 * $Id: AXdrCoderSequenceOfWrapper.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:37:48
 */
package com.elster.axdr.coding;

import com.elster.coding.AbstractCoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class wraps another encoder as an "sequence of" part.<P>
 *
 * @author osse
 */
public class AXdrCoderSequenceOfWrapper<T> extends AbstractAXdrCoder<List<T>>
{
  private final AbstractCoder<T> wrapped;

  /**
   * Constructor of the "sequence of" wrapper.<P>
   *
   * @param wrapped The optional part of a PDU (protocol data unit)
   */
  public AXdrCoderSequenceOfWrapper(final AbstractCoder<T> wrapped)
  {
    super();
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
   * Encodes the objects.<P>
   *
   * @param objects List of objects to encode
   * @param out The output stream to encode the objects to.
   * @throws IOException
   */
  @Override
  public void encodeObject(final List<T> objects, final AXdrOutputStream out) throws IOException
  {
    out.writeLength(objects.size());

    for (T o : objects)
    {
      wrapped.encodeObject(o, out);
    }
  }

  /**
   * Reads and decodes objects from the input stream.<P>
   *
   * @param in The input stream
   * @return The decoded objects
   * @throws IOException
   */
  @Override
  public List<T> decodeObject(final AXdrInputStream in) throws IOException
  {
    final int length= in.readLength();
    final List<T> result= new ArrayList<T>(length);
    
    for (int i=0; i<length; i++)
    {
      result.add(wrapped.decodeObject(in));
    }
    return result;
  }

}
