/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/errortest/ErrorTestDelayOutputStream.java $
 * Version:     
 * $Id: ErrorTestDelayOutputStream.java 3826 2011-12-09 10:07:51Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.07.2011 14:02:16
 */
package com.elster.protocols.errortest;

import com.elster.coding.CodingUtils;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This output stream manipulates bits of an output stream for testing purposes.
 * <P>
 * It can be used to simulate a bad modem connection.
 *
 * @author osse
 */
public class ErrorTestDelayOutputStream extends FilterOutputStream
{
  private final long delay;
  private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);

  public ErrorTestDelayOutputStream(OutputStream out, int delay)
  {
    super(out);
    this.delay = delay;
  }

  @Override
  public void write(int b) throws IOException
  {
    //out.write(b);
    executorService.schedule(new DataBlock(b), delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    executorService.schedule(new DataBlock(CodingUtils.copyOfRange(b, off, len)), delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void write(byte[] b) throws IOException
  {
    executorService.schedule(new DataBlock(b.clone()), delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void close() throws IOException
  {
    try
    {
      try
      {
        executorService.shutdown();
        executorService.awaitTermination(3L * delay, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException ignore)
      {
        Thread.currentThread().interrupt(); //restore state.
      }
    }
    finally
    {
      out.close();
    }
  }

  private static final byte[] EMPTY = new byte[0];

  @Override
  public void flush() throws IOException
  {
    executorService.schedule(new DataBlock(EMPTY), delay, TimeUnit.MILLISECONDS);
  }

  private class DataBlock implements Runnable
  {
    private final byte[] bytes;

    public DataBlock(final byte[] bytes)
    {
      this.bytes = bytes;
    }

    public DataBlock(final int oneByte)
    {
      this.bytes = new byte[]
      {
        (byte)oneByte
      };
    }

    public void run()
    {
      try
      {
        synchronized (out)
        {
          if (bytes.length > 0)
          {
            out.write(bytes);
          }
          out.flush();
        }
      }
      catch (IOException ex)
      {
        Logger.getLogger(ErrorTestDelayOutputStream.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }

}
