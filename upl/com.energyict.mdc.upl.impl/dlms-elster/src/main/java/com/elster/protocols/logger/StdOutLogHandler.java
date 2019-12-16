/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/logger/StdOutLogHandler.java $
 * Version:     
 * $Id: StdOutLogHandler.java 4878 2012-07-20 08:38:05Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 21, 2010 9:28:05 AM
 */
package com.elster.protocols.logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This LogHandler logs the data directly to System.out.
 * <P>
 * The logger will start a new line after a pause of 200ms or if the direction changes.<br>
 * Read data will only be logged if it is requested by the overlying layers.
 * <P>
 * The log data will be written by a separate thread.
*
 * @author osse
 */
public class StdOutLogHandler implements ILogHandler
{
  private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
  private final BlockingQueue<LogPacket> logPackets = new LinkedBlockingQueue<LogPacket>();
  private Thread loggingThread = null;
  private final LogPacketComment poisson = new LogPacketComment(LogDirection.NONE, new Date(), "");

  public void open() throws IOException
  {
    loggingThread = new Thread(new LogRunnable());
    loggingThread.setName("Protocol logger");
    loggingThread.setDaemon(true);
    loggingThread.start();
  }

  public void close() throws IOException
  {
    try
    {
      logPacket(poisson);
      loggingThread.join();
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(AsyncLogHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void logPacket(LogPacket logPacket) throws IOException
  {
    try
    {
      logPackets.put(logPacket);
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(StdOutLogHandler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private class LogRunnable implements Runnable
  {
    public void run()
    {
      try
      {
        LogPacket firstPacket = null;
        while (true)
        {
          if (firstPacket == null)
          {
            firstPacket = logPackets.take();
          }

          if (firstPacket == poisson)
          {
            break;
          }

          StringBuilder sb = new StringBuilder();
          try
          {
            sb.append(dateFormat.format(firstPacket.getTimestamp()));
            switch (firstPacket.getDirection())
            {
              case IN:
                sb.append(" < ");
                break;
              case OUT:
                sb.append(" > ");
                break;
              case NONE:
                sb.append("   ");
                break;
            }
            sb.append(firstPacket.getInfo());

            //--- Datenpakete zusammenfassen ---
            if (firstPacket.getPacketType() == LogPacket.PacketType.DATA)
            {
              while (true)
              {
                LogPacket packet = logPackets.poll(200, TimeUnit.MILLISECONDS);
                if (packet == null)
                {
                  firstPacket = null;
                  break;
                }

                if (packet == poisson)
                {
                  firstPacket = packet;
                  break;
                }

                if (packet.getDirection() != firstPacket.getDirection() || packet.getPacketType()
                                                                           != LogPacket.PacketType.DATA)
                {
                  firstPacket = packet;
                  break;
                }
                else
                {
                  sb.append(" ");
                  sb.append(packet.getInfo());
                }
              }
            }
            else
            {
              firstPacket = null;
            }
          }
          finally
          {
            sb.append(" |");
            System.out.println(sb.toString());
          }
        }
      }
      catch (InterruptedException ignore)
      {
      }
    }

  }

}
