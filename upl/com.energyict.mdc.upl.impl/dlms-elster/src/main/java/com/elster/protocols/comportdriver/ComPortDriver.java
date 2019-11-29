/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/comportdriver/ComPortDriver.java $
 * Version:     
 * $Id: ComPortDriver.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.04.2010 16:50:49
 */
package com.elster.protocols.comportdriver;

import com.elster.coding.CodingUtils;
import com.elster.protocols.*;
import com.elster.protocols.logger.ILogHandler;
import com.elster.protocols.logger.LogDirection;
import com.elster.protocols.streams.IByteSink;
import com.elster.protocols.streams.IntervalInputStreamPipe;
import com.elster.protocols.streams.TimeoutableOutputStreamPipe;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Driver for accessing an ComPort.
 * <P>
 * This class uses the {@link JniComPortConnection} class to provide access to one COM port.
 * <P>
 * Data can be written by the output stream (see {@link #getOutputStream}) and
 * be readTO be the input stream (see {@link #getInputStream})
 * <P>
 * The data format will be always 8N1. 7E1 support can be achieved by using the {@link com.elster.protocols.dataformat.ProtocolDataFormat} layer
 * or the data format streams {@link com.elster.protocols.dataformat.DataFormatInputStream} and {@link com.elster.protocols.dataformat.DataFormatOutputStream}.
 *
 * @author osse
 */
public class ComPortDriver extends AbstractProtocol implements IBaudrateSupport, Closeable, IStreamProtocol
{
  private static final Logger LOGGER = Logger.getLogger(ComPortDriver.class.getName());
  private JniComPortConnection connection;
  private final InputStream publicInputStream;
  private OutputStream receiveOutputStream;
  private final OutputStream publicOutputStream = new ConnectionOutputStream();
  private Thread receivingThread;
  private int comPort = 1;
  private int baudrate = 9600;
  private RtsControl rtsControl = RtsControl.ENABLE;
  private boolean outCtsFlow = false;
  private ILogHandler fileLogger = null;

  public enum RtsControl
  {
    DISABLE, ENABLE, HANDSHAKE, TOGGLE
  };

//  public ComPortDriver()
//  {
//    publicInputStream = new TimeoutableInputStreamPipe();
//  }
  /**
   * Creates the ComPortDriver<P>
   *
   * @param interval The read methods of the input stream, returned by {@link #getInputStream()}, are returning after {@code interval} milliseconds
   * if no data or only a part of the requested data was received.
   *
   */
  public ComPortDriver(final int interval)
  {
    publicInputStream = new IntervalInputStreamPipe(interval);
  }

  /**
   * Opens the COM port.
   *
   * @throws IOException
   */
  //@Override
  public void open() throws IOException
  {
    if (isOpen())
    {
      throw new IllegalStateException("COM Port is already open");
    }

    if (fileLogger != null)
    {
      fileLogger.open();
    }


    setProtocolState(ProtocolState.OPENING);

    boolean open = false;

    try
    {

      synchronized (this)
      {

        connection = new JniComPortConnection("COM" + comPort);
        connection.open(comPort, baudrate, rtsControl.ordinal(), outCtsFlow);
        open = true;
        try
        {
          receiveOutputStream = new TimeoutableOutputStreamPipe((IByteSink)publicInputStream);
          startReceiving();
        }
        catch (IOException ex)
        {
          connection.tryClose();
          throw ex;
        }
      }
    }
    finally
    {
      if (open)
      {
        setProtocolState(ProtocolState.OPEN);
      }
      else
      {
        setProtocolState(ProtocolState.CLOSE);
      }

    }
  }

  /**
   * Closes the COM port.
   * <P>
   * Closes an open COM port. If the COM port is already closed, nothing will be done.
   *
   * @throws IOException
   */
  //@Override
  public void close() throws IOException
  {
    if (isOpen())
    {
      setProtocolState(ProtocolState.CLOSING);
      synchronized (this)
      {
        connection.cancelReceiving();
        try
        {
          receivingThread.join();
        }
        catch (InterruptedException ex)
        {
          Logger.getLogger(ComPortDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

        try
        {
          receiveOutputStream.close();
        }
        catch (IOException ex)
        {
          Logger.getLogger(ComPortDriver.class.getName()).log(Level.SEVERE, null, ex);
        }

        connection.close();
        if (fileLogger != null)
        {
          fileLogger.close();
        }
      }
      setProtocolState(ProtocolState.CLOSE);
    }
  }

  private synchronized void send(byte[] data) throws IOException
  {
    checkOpen();
    if (fileLogger != null)
    {
      fileLogger.logPacket(new ILogHandler.LogPacketData(LogDirection.OUT, data));
    }
    connection.send(data);
  }

  private synchronized void startReceiving() throws IOException
  {
    connection.setOnReceivedHandler(new JniComPortConnection.OnReceivedHandler()
    {
      //@Override
      public void onDataReceived(byte[] data)
      {
        try
        {
          if (fileLogger != null)
          {
            fileLogger.logPacket(new ILogHandler.LogPacketData(LogDirection.IN,  data));
            fileLogger.logPacket(new ILogHandler.LogPacketComment(LogDirection.IN, "[" + data.length + " Bytes]"));
          }
          receiveOutputStream.write(data);
          receiveOutputStream.flush();
        }
        catch (IOException ex)
        {
          Logger.getLogger(ComPortDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    });

    receivingThread = new Thread(new Runnable()
    {
      //@Override
      public void run()
      {
        try
        {
          connection.startReceiving();
        }
        catch (IOException ex)
        {
          Logger.getLogger(ComPortDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

    });
    receivingThread.setDaemon(true);
    receivingThread.setName("Serial port receiving thread");
    receivingThread.start();
  }

  /**
   * Returns an input stream to receive data from the COM-Port.<P>
   * The input stream additionally supports readTO methods with an timeout parameter.
   *
   * @return The input stream.
   */
  //@Override
  public InputStream getInputStream()
  {
    return publicInputStream;
  }

  /**
   *  Returns an output stream to write data to the comport.
   *
   * @return The output stream
   */
  //@Override
  public OutputStream getOutputStream()
  {
    return publicOutputStream;
  }

  /**
   * Returns the current baud rate.
   *
   * @return The baud rate.
   */
  //@Override
  public synchronized int getBaudrate()
  {
    return baudrate;
  }

  /**
   * Set the baud rate.<P>
   * If the COM port is open the baud rate will be changed. <br>
   * If the COM port is closed the baud rate will be used by the next call to {@link #open}.
   *
   * @param baudrate The new baud rate.
   * @throws IOException
   */
  //@Override
  public synchronized void setBaudrate(final int baudrate) throws IOException
  {
    if (this.baudrate != baudrate)
    {
      if (isOpen())
      {
        connection.changeBaudrate(baudrate);
      }
      this.baudrate = baudrate;
    }
  }

  /**
   * Returns the number of the COM port.
   *
   * @return the number of the COM port.
   */
  public synchronized int getComPort()
  {
    return comPort;
  }

  /**
   * Set the number of the COM port.
   * <P>
   * The number can only be set if the COM port is not open (by this instance). Otherwise an
   * IllegalStateException will be thrown.
   * 
   * @param comPort The number of the COM port.
   */
  public synchronized void setComPort(int comPort)
  {
    if (isOpen())
    {
      throw new IllegalStateException("The com port cannot be changed during an open connection");
    }

    this.comPort = comPort;
  }

  /**
   * Returns the RTS control mode<P>
   * Default value: {@link RtsControl#ENABLE }.
   *
   * @return The RTS control mode
   */
  public synchronized RtsControl getRtsControl()
  {
    return rtsControl;
  }

  /**
   * Set the RTS control mode.<P>
   * Can only be changed if the connection is closed.
   *
   * @param rtsControl The RTS control mode.
   */
  public synchronized void setRtsControl(RtsControl rtsControl)
  {
    if (isOpen())
    {
      throw new IllegalStateException("The rts control cannot be changed during an open connection");
    }
    this.rtsControl = rtsControl;
  }

  /**
   * The CTS flow control.<P>
   * Default value is {@code false}.
   *
   * @return The CTS flow control.
   */
  public synchronized boolean isOutCtsFlow()
  {
    return outCtsFlow;
  }

  /**
   * Enables or disables the (outgoing) CTS flow control. <P>
   * Can only be changed if the connection is closed.
   *
   * @param outCtsFlow {@code true} to enable, {@code false} to disable the CTS flow control
   */
  public synchronized void setOutCtsFlow(boolean outCtsFlow)
  {
    this.outCtsFlow = outCtsFlow;
  }

  private void checkOpen() throws IllegalProtocolStateException
  {
    checkProtocolState(ProtocolState.OPEN);
  }

  private class ConnectionOutputStream extends OutputStream
  {
    @Override
    public void write(int b) throws IOException
    {
      send(new byte[]
              {
                (byte)b
              });
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
      if ((off == 0) && (b.length == len))
      {
        send(b);
      }
      else
      {
        send(CodingUtils.copyOfRange(b, off, off + len));
      }
    }

    @Override
    public void write(byte[] b) throws IOException
    {
      send(b);
    }

  }

  @Override
  protected void finalize() throws Throwable
  {
    if (isOpen())
    {
      LOGGER.severe("Closing COM Port in finalize");
      close();
    }
    super.finalize();
  }

  public ILogHandler getFileLogger()
  {
    return fileLogger;
  }

  public void setFileLogger(final ILogHandler fileLogger)
  {
    this.fileLogger = fileLogger;
  }


  JniComPortConnection getConnection()
  {
    return connection;
  }

}
