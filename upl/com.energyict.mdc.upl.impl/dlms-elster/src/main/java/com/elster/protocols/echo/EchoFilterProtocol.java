/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/echo/EchoFilterProtocol.java $
 * Version:     
 * $Id: EchoFilterProtocol.java 4874 2012-07-19 15:23:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.08.2010 15:00:35
 */
package com.elster.protocols.echo;

import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.streams.CircularByteBuffer;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Protocol which filters an echo from the output stream in half duplex communications.
 * <P>
 * This protocol expects that every byte written to the output stream of the sublayer will be echoed
 * to the input stream of the sublayer. These echoed bytes will be filtered (removed). <P>
 * The filtering is done during reading of the answer. The overlying layer must read the complete answer before
 * new bytes are send.<P>
 * The echo will be checked. If the check fails the echo buffer will be cleared and some bytes
 * of the non echo part will probably be lost. No exception will be thrown. This behavior is fast and should have
 * no disadvantages with "save" protocols like the HLDC Protocol.
 *
 * @author osse
 */
public class EchoFilterProtocol extends FilterProtocol
{
  /*
   * Alternative Möglichkeiten der Implementierung:
   *
   * 1. Eine andere mögliche Implementierung wäre direkt beim Schreiben das Echo zu lesen
   * und erst nach dem Empfang des Echos die Senderoutine zu verlassen (das wäre deutlich langsamer).
   *
   * 2. Das Lesen des Echos (auch) auf die Flush-Routine zu verlagern
   *
   */
  private final EchoInputStream in;
  private final EchoOutputStream out;


  /**
   * Constructor with sub layer.<P>
   *
   * @param sublayer
   */
  public EchoFilterProtocol(IStreamProtocol sublayer)
  {
    super(sublayer);
    in = new EchoInputStream(sublayer.getInputStream());
    out = new EchoOutputStream(sublayer.getOutputStream());
  }

  @Override
  public InputStream getInputStream()
  {
    return in;
  }

  @Override
  public OutputStream getOutputStream()
  {
    return out;
  }

  private final CircularByteBuffer echoBuffer = new CircularByteBuffer();

  private class EchoInputStream extends FilterInputStream
  {
    public EchoInputStream(InputStream in)
    {
      super(in);
    }

    @Override
    public int available() throws IOException
    {
      synchronized (echoBuffer)
      {
        int available = in.available() - echoBuffer.size();

        if (available > 0)
        {
          return available;
        }
        else
        {
          return 0;
        }
      }
    }

    @Override
    public boolean markSupported()
    {
      return false;
    }

    @Override
    public int read() throws IOException
    {
      readEcho();
      return in.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      readEcho();
      return in.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException
    {
      readEcho();
      return in.skip(n);
    }

    private final byte[] readBuffer = new byte[64];

    private int readEcho() throws IOException
    {
      synchronized (echoBuffer)
      {
        while (echoBuffer.size() > 0)
        {
          int bytesRead = in.read(readBuffer, 0, Math.min(echoBuffer.size(), readBuffer.length));
          if (bytesRead == -1)
          {
            return -1;
          }

          for (int i = 0; i < bytesRead; i++)
          {
            if ( (readBuffer[i] & 0xFF) != echoBuffer.read())
            {
              echoBuffer.clear();
              break;
            }
          }
        }
      }
      return 0;
    }

  }

  private class EchoOutputStream extends FilterOutputStream
  {
    public EchoOutputStream(OutputStream out)
    {
      super(out);
    }

    @Override
    public void write(int b) throws IOException
    {
      synchronized (echoBuffer)
      {
        echoBuffer.write(b);
      }
      out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
      synchronized (echoBuffer)
      {
        echoBuffer.write(b, off, len);
      }
      out.write(b, off, len);
    }
  }

}
