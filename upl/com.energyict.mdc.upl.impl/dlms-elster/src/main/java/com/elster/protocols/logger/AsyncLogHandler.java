/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/logger/AsyncLogHandler.java $
 * Version:     
 * $Id: AsyncLogHandler.java 4878 2012-07-20 08:38:05Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 21, 2010 9:16:06 AM
 */
package com.elster.protocols.logger;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Log handler which writes log entries in background to a sublayer, by a separate thread.
 *
 * @author osse
 */
public class AsyncLogHandler implements ILogHandler
{
  private final BlockingQueue<LogPacket> logPackets = new LinkedBlockingQueue<LogPacket>();
  private Thread loggingThread = null;
  private final ILogHandler subHandler;
  private final LogPacketComment poison = new LogPacketComment(LogDirection.NONE, new Date(), "");

  public AsyncLogHandler(final ILogHandler subHandler)
  {
    this.subHandler = subHandler;
  }

  public void logPacket(final LogPacket logPacket) throws IOException
  {
    try
    {
      logPackets.put(logPacket);
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(AsyncLogHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private class LogRunnable implements Runnable
  {
    public void run()
    {
      try
      {
        while (true)
        {
          final LogPacket packet = logPackets.take();
          if (packet == poison)
          {
            break;
          }
          subHandler.logPacket(packet);
        }
        subHandler.close();
      }
      catch (IOException ex)
      {
        Logger.getLogger(AsyncLogHandler.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (InterruptedException ex)
      {
      }
    }

  }

  public void open() throws IOException
  {
    subHandler.open();
    loggingThread = new Thread(new LogRunnable());
    loggingThread.setName("Protocol logger");
    loggingThread.setDaemon(true);
    loggingThread.start();
  }

  public void close() throws IOException
  {
    try
    {
      logPacket(poison);
      loggingThread.join();
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(AsyncLogHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
