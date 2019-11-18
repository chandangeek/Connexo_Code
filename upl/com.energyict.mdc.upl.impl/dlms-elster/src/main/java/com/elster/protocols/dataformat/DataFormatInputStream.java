/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/dataformat/DataFormatInputStream.java $
 * Version:     
 * $Id: DataFormatInputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 09:38:55
 */
package com.elster.protocols.dataformat;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class check the dataformat (7E1) of input stream and clears the parity bit from
 * the data.
 * <P>
 * Currently only 7E1 is supported.
 * <P>
 * If an parity error is detected the {@link DataFormatIOException} is thrown.
 * <P>
 * The block read methods will try to read the requested amount of data from the sublayer,
 * and check and convert the data afterwards. This means if an parity exeption occurs, the data after
 * the faulty byte will be probably allready read from the sublayer and will not be available
 * by the next call of read anymore.<br>
 * This behaviour may be changed in further versions of this class.
 *
 *
 * @author osse
 */
public class DataFormatInputStream extends FilterInputStream
{
  private final byte[] lookupTable = Table7E1.getTable();

  private boolean checkByte(int byteToCheck)
  {
    return (0xFF & lookupTable[byteToCheck & 0x7F]) == byteToCheck;
  }

  private boolean checkByte(byte byteToCheck)
  {
    return (lookupTable[byteToCheck & 0x7F]) == byteToCheck;
  }

  private void checkAndConvertBytes(byte[] b, int off, int len) throws DataFormatIOException
  {

    boolean error = false;
    for (int i = 0; i < len; i++)
    {
      if (!checkByte(b[i + off]))
      {
        error = true;
        break;
      }
    }

    for (int i = 0; i < len; i++)
    {
      b[i + off] = (byte)(0x7F & b[i + off]);
    }

    if (error)
    {
      throw new DataFormatIOException();
    }
  }

  public DataFormatInputStream(InputStream in)
  {
    super(in);
  }

  @Override
  public int read() throws IOException
  {
    int result = in.read();
    if (!checkByte(result))
    {
      throw new DataFormatIOException();
    }
    return result & 0x7F;
  }

  @Override
  public int read(byte[] b) throws IOException
  {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException
  {
    int count = in.read(b, off, len);
    checkAndConvertBytes(b, off, count);
    return count;
  }

}
