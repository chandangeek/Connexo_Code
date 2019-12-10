/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/axdr/coding/AbstractAXdrCoder.java $
 * Version:     
 * $Id: AbstractAXdrCoder.java 2567 2011-01-21 14:30:26Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 19:07:35
 */
package com.elster.axdr.coding;

import com.elster.coding.AbstractCoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract base class for AXdr en-/decoders.
 *
 * @author osse
 */
public abstract class AbstractAXdrCoder<T> extends AbstractCoder<T>
{
  @Override
  public final void encodeObject(final T object,final OutputStream out) throws IOException
  {
    if (out instanceof AXdrOutputStream)
    {
      encodeObject(object, (AXdrOutputStream)out);
    }
    else
    {
      encodeObject(object, new AXdrOutputStream(out));
    }
  }

  @Override
  public final T decodeObject(final InputStream in) throws IOException
  {
    if (in instanceof AXdrInputStream)
    {
      return decodeObject((AXdrInputStream)in);
    }
    else
    {
      return decodeObject(new AXdrInputStream(in));
    }
  }

  /**
   * Encodes an object and writes it to the specified output stream.
   *
   * @param object The object to encode.
   * @param out The {@link AXdrOutputStream } to write the object to.
   * @throws IOException
   */
  public abstract void encodeObject(final T object, final AXdrOutputStream out) throws IOException;

  /**
   * Reads an object from the specified input stream and decodes it.
   *
   * @param in The {@link AXdrInputStream } to read the object from
   * @return The decoded object.
   * @throws IOException
   */
  public abstract T decodeObject(final AXdrInputStream in) throws IOException;


   /**
   * Encodes the specified object to an byte array.<P>
   * The prefix will be the first byte.
   *
   * @param object The object to encode.
   * @return The byte array
   * @throws IOException
   */
  public final byte[] encodeObjectToBytes(int prefix, T object) throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(prefix);
    encodeObject(object, out);
    return out.toByteArray();
  }


}
