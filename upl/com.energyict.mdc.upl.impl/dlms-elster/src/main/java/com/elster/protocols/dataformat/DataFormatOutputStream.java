/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/dataformat/DataFormatOutputStream.java $
 * Version:     
 * $Id: DataFormatOutputStream.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 11:21:58
 */
package com.elster.protocols.dataformat;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * This class apply 7E1 parity bits to the output stream.<P>
 * Further data formats may be added in future versions of this class.
 *
 * @author osse
 */
public class DataFormatOutputStream extends FilterOutputStream
{
  private final byte[] lookupTable = Table7E1.getTable();

  public DataFormatOutputStream(OutputStream out)
  {
    super(out);
  }

  @Override
  public void write(int b) throws IOException
  {
    if (b > 127)
    {
      throw new InvalidParameterException("b must be smaller than 128");
    }
    out.write(lookupTable[b]);
  }

  @Override
  public void write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    for (int i = 0; i < len; i++)
    {
      if ((0x80 & b[i + off]) != 0)
      {
        throw new InvalidParameterException("all elements in b must be smaller than 128");
      }
    }

    byte[] buffer = new byte[len];

    for (int i = 0; i < len; i++)
    {
      buffer[i] = lookupTable[0xFF & b[i + off]];
    }
    out.write(buffer, 0, len);

  }

}
