/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/SimpleInputStreamMultiplexer.java $
 * Version:     
 * $Id: SimpleInputStreamMultiplexer.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.05.2010 14:59:02
 */
package com.elster.protocols.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class copies the input from one input stream to a predefined number of other inputstream.
 * <P>
 * The implementation is very simple. It uses a TimeoutableInputStreamPipe for each stream and
 * copies the with an own thread to these streams from the source stream.<br>
 * This means a separate exists buffer for each input stream. (A better implementation
 * should only use one buffer)
 * <P>
 * The <b>{@link #open()}</b> method must be called to start the reading from the source stream and the
 * dispatching to the streams.
 *
 * @author osse
 */
public class SimpleInputStreamMultiplexer
{
  private final InputStream source;
  private final TimeoutableInputStreamPipe[] ins;
  private final TimeoutableOutputStreamPipe[] outs;

  public SimpleInputStreamMultiplexer(InputStream source, int count)
  {
    this.source = source;

    ins = new TimeoutableInputStreamPipe[count];
    outs = new TimeoutableOutputStreamPipe[count];

    for (int i = 0; i < count; i++)
    {
      ins[i] = new TimeoutableInputStreamPipe();
      ins[i].setTimeout(0);
      ins[i].setTotalTimeout(0);
      outs[i] = new TimeoutableOutputStreamPipe(ins[i]);

    }
  }

  public TimeoutableInputStreamPipe getInputStream(int index)
  {
    return ins[index];
  }

  Dispatcher dispatcher;

  public void open()
  {
    dispatcher = new Dispatcher();
    dispatcher.start();
  }

  public void close()
  {
    try
    {
      dispatcher.interrupt();
      dispatcher.join();
      for (TimeoutableOutputStreamPipe o : outs)
      {
        o.close();
      }
    }
    catch (InterruptedException ex)
    {
      Logger.getLogger(InputStreamMultiplexer.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (IOException ex)
    {
      Logger.getLogger(InputStreamMultiplexer.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private class Dispatcher extends Thread
  {
    public Dispatcher()
    {
      super("Inputstream multiplexer");
    }

    @Override
    public void run()
    {
      try
      {
        byte buffer[] = new byte[1024];

        while (!isInterrupted())
        {
          if (source.available() == 0)
          {
            int singleByte = source.read();
            int available = source.available();
            if (singleByte < 0)
            {
              for (TimeoutableOutputStreamPipe o : outs)
              {
                o.close();
              }
              break;
            }

            for (TimeoutableOutputStreamPipe o : outs)
            {
              o.write(singleByte);
              if (available == 0)
              {
                o.flush();
              }
            }
          }

          int available = source.available();

          while (available > 0)
          {
            int s = Math.min(available, buffer.length);
            int bytesRead = source.read(buffer, 0, s);
            for (TimeoutableOutputStreamPipe o : outs)
            {
              o.write(buffer, 0, bytesRead);
              o.flush();
            }
            available = source.available();
          }
        }
      }
      catch (IOException ex)
      {
        Logger.getLogger(InputStreamMultiplexer.class.getName()).log(Level.SEVERE, null, ex);
      }

    }

  }

}
