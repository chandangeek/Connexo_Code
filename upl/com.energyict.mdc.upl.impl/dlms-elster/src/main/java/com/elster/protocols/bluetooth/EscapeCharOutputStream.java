/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/bluetooth/EscapeCharOutputStream.java $
 * Version:     
 * $Id: EscapeCharOutputStream.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 11:21:58
 */
package com.elster.protocols.bluetooth;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This output stream simply doubles one specific byte.
 * 
 * @author osse
 */
public class EscapeCharOutputStream extends FilterOutputStream
{
  private final byte escapeByte;

  public EscapeCharOutputStream(final OutputStream out, int escapeByte)
  {
    super(out);
    this.escapeByte = (byte)escapeByte;
  }

  @Override
  public void write(final int b) throws IOException
  {
    out.write(b);
    if (b == escapeByte)
    {
      out.write(escapeByte);
    }
  }

  @Override
  public void write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException
  {
    int escapeCount = 0;

    for (int i = 0; i < len; i++)
    {
      if (b[i + off] == escapeByte)
      {
        escapeCount++;
      }
    }

    if (escapeCount == 0)
    {
      out.write(b, off, len);
    }
    else
    {
      byte[] buffer = new byte[len + escapeCount];
      int pos = 0;
      for (int i = 0; i < len; i++)
      {
        buffer[pos] = b[i + off];
        pos++;
        if (b[i + off] == escapeByte)
        {
          escapeCount++;
          buffer[pos] = escapeByte;
          pos++;
        }
      }
      out.write(buffer);
    }




  }

}
