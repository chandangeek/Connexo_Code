/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/coding/AbstractCoder.java $
 * Version:     
 * $Id: AbstractCoder.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  16.04.2010 16:18:47
 */
package com.elster.coding;

import java.io.*;

/**
 * Abstract base class for decoders and encoders of objects.
 *
 * @author osse
 */
public abstract class AbstractCoder<T extends Object>
{
  /**
   * Encodes the specified object to the output stream.
   *
   * @param object The object to encode
   * @param out The output stream
   * @throws IOException
   */
  public abstract void encodeObject(T object, OutputStream out) throws IOException;

  /**
   * Encodes the specified object to an byte array.<P> Internally {@link #encodeObject(java.lang.Object, java.io.OutputStream)}
   * will be used.
   *
   * @param object The object to encode.
   * @return The byte array
   * @throws IOException
   */
  public final byte[] encodeObjectToBytes(final T object) throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    encodeObject(object, out);
    return out.toByteArray();
  }

  /**
   * Decodes an object of type <b>{@code T}</b> from the specified InputStream.
   *
   * @param in The input stream.
   * @return The decoded object.
   * @throws IOException
   */
  public abstract T decodeObject(final InputStream in) throws IOException;

  /**
   * Decodes the data to an object.<P> All bytes must be used or an IOException will be thrown.
   *
   * @param data The data to decode.
   * @return The decoded object.
   * @throws IOException
   */
  public final T decodeObjectFromBytes(final byte[] data) throws IOException
  {
    return decodeObjectFromBytes(data, 0, data.length);
  }

  /**
   * Decodes the data to an object.<P> All bytes (respecting offset and length) must be used or an IOException
   * will be thrown.
   *
   * @param data The data to decode.
   * @param offset The offset in the data array
   * @param length The length of data to decode.
   * @return The decoded object.
   * @throws IOException
   */
  public final T decodeObjectFromBytes(final byte[] data, final int offset, final int length) throws
          IOException
  {
    ByteArrayInputStream in = new ByteArrayInputStream(data, offset, length);
    T o = decodeObject(in);

    if (in.available() > 0)
    {
      throw new IOException("Decoding Object: " + in.available() + " Bytes left.");
    }

    in.close();
    return o;
  }

}
