/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/logger/LoggingProtocol.java $
 * Version:     
 * $Id: LoggingProtocol.java 4901 2012-07-27 13:22:08Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Sep 17, 2010 11:12:40 AM
 */
package com.elster.protocols.logger;

import com.elster.coding.CodingUtils;
import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.logger.ILogHandler.LogPacket;
import com.elster.protocols.logger.ILogHandler.LogPacketData;
import java.io.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logging protocol layer.<P>
 *
 * @author osse
 */
public class LoggingProtocol extends FilterProtocol
{
  private final InputStream in;
  private final OutputStream out;
  private final ILogHandler logHandler;
  
  private boolean logHandlerOpen;
    

  public LoggingProtocol(final IStreamProtocol sublayer, final ILogHandler logHandler)
  {
    super(sublayer);
    in = new LoggingInputStream(sublayer.getInputStream());
    out = new LoggingOutputStream(sublayer.getOutputStream());
    this.logHandler = logHandler;
    if (sublayer.isOpen())
    {
      try
      {
        logHandler.open();
        logHandlerOpen=true;
      }
      catch (IOException ex)
      {
        Logger.getLogger(LoggingProtocol.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
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

  @Override
  public void close() throws IOException
  {
    super.close();
    logHandler.close();
  }

  @Override
  public void open() throws IOException
  {
    if (!logHandlerOpen)
    {
      logHandler.open();
      logHandlerOpen=true;
    }
    super.open();
  }

  private void logPacket(final LogPacket packet) throws IOException
  {
    logHandler.logPacket(packet);
  }

  private class LoggingInputStream extends FilterInputStream
  {
    public LoggingInputStream(final InputStream in)
    {
      super(in);
    }

    @Override
    public int read() throws IOException
    {
      final int result = super.read();
      if (result >= 0)
      {
        logPacket(
                new LogPacketData(LogDirection.IN, new Date(), new byte[]
                {
                  (byte)result
                }));
      }
      return result;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException
    {
      int result = super.read(b, off, len);

      if (result > 0)
      {
        logPacket(
                new LogPacketData(LogDirection.IN, new Date(), CodingUtils.copyOfRange(b, off, off + result)));
      }
      return result;
    }

  }

  private class LoggingOutputStream extends FilterOutputStream
  {
    public LoggingOutputStream(final OutputStream out)
    {
      super(out);
    }

    @Override
    public void write(final int b) throws IOException
    {
      logPacket(
              new LogPacketData(LogDirection.OUT, new Date(), new byte[]
              {
                (byte)b
              }));
      out.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException
    {
      logPacket(new LogPacketData(LogDirection.OUT, new Date(), CodingUtils.copyOfRange(b, off, off + len)));
      out.write(b, off, len);
    }

  }

}
