/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/comportdriver/JniComPortConnection.java $
 * Version:     
 * $Id: JniComPortConnection.java 5724 2012-12-11 15:07:48Z SchulteM $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.04.2010 11:34:11
 */
package com.elster.protocols.comportdriver;

import com.elster.protocols.exceptions.LocalInterfaceException;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This class uses the "comportcon.dll" to establish a connection through a
 * COM port.
 * <P>
 * This class is designed to be used by the {@link ComPortDriver}
 * <P>
 * To set the library path in java the option "-Djava.library.path=d:/pathtodlls" can be used
 * 
 * @author osse
 */
public class JniComPortConnection
{
  private static final int ERROR_CREATE_FILE = -1;
  private static final int ERROR_SET_COMM_STATE = -2;
  private static final int ERROR_GET_TIMEOUTS = -3;
  private static final int ERROR_SET_TIMEOUTS = -4;
  public static final int RTS_CONTROL_DISABLE = 0x00;
  public static final int RTS_CONTROL_ENABLE = 0x01;
  public static final int RTS_CONTROL_HANDSHAKE = 0x02;
  public static final int RTS_CONTROL_TOGGLE = 0x03;
  private OnReceivedHandler onReceivedHandler;
  private long lastError = 0; //Do not delete! Used by the driver dll. Wird von der Treiber-DLL gesetzt und gelesen.
  private int driverHandle = 0;//Do not delete! Used by the driver dll. Wird von der Treiber-DLL gesetzt und gelesen.
  private final SendOverlappedDataExecutor overlappedSendExecutor;

  private static native void navtiveInitIDs();

  private native int nativeOpen(int comPort, int baudRate, int rtsControl, int outCtsFlow);

  private native int nativeClose();

  private native int nativeChangeBaudrate(int newBaudRate);

  private native int nativeSend(byte[] data);

  private native int nativeOverlappedSend(byte[] data);

  private native int nativeStartReceiving();

  private native int nativeTrigger();

  private native int nativeGetBytesInTxBuffer();

  private final String interfaceName;

  public JniComPortConnection(final String interfaceName)
  {
    this.interfaceName = interfaceName;
    this.overlappedSendExecutor = new SendOverlappedDataExecutor();
  }

  /**
   * Opens the specified COM port.
   *
   * @param comPort The number of the COM Port to open.
   * @param baudRate The baud rate.
   * @param rtsControl The RTS Control mode. (One of {@code RTS_CONTROL_DISABLE, RTS_CONTROL_ENABLE, RTS_CONTROL_HANDSHAKE, RTS_CONTROL_TOGGLE}).
   * @param outCtsFlow {@code true} to enable the CTS flow control (TX)
   * @throws IOException
   */
  public void open(int comPort, int baudRate, int rtsControl, boolean outCtsFlow) throws IOException
  {
    checkForError(nativeOpen(comPort, baudRate, rtsControl, outCtsFlow ? 1 : 0));
  }

  /**
   * Closes the COM port
   *
   * @throws IOException
   */
  public void close() throws IOException
  {
    checkForError(nativeClose());
  }

  /**
   * Tries to close the COM port.<P>
   * No exception will be thrown if an error occurs.
   *
   * @return {@code false} if an error occurred. Otherwise {@code true}.
   */
  public boolean tryClose()
  {
    return nativeClose() < 0;
  }

  /**
   * Sends a block of data.
   *
   * @param data The data to send.
   * @throws IOException
   */
  public void send(byte[] data) throws IOException
  {
    checkForError(nativeSend(data));
    //executeEvent(new SendDataExecutor(data));


//    synchronized (overlappedSendLock)
//    {
//      sendFinished = false;
//      overlappedSendExecutor.setData(data);
//      executeEvent(overlappedSendExecutor);
//      while (!sendFinished)
//      {
//        try
//        {
//          overlappedSendLock.wait();
//        }
//        catch (InterruptedException ex)
//        {
//          throw new InterruptedIOException(ex.getMessage());
//        }
//      }
//      overlappedSendExecutor.setData(null);
//    }
  }

  /**
   * Changes the baud rate of an open COM port.
   *
   * @param newBaudrate The baud rate to set.
   * @throws IOException
   */
  public void changeBaudrate(int newBaudrate) throws IOException
  {
    checkForError(nativeChangeBaudrate(newBaudrate));
  }

  /**
   * Starts the receiving loop.<P>
   * The loop exits if {@link #cancelReceiving} was called.
   * Is method is designed to be called from a separate thread.
   *
   * @throws IOException
   */
  public void startReceiving() throws IOException
  {
    checkForError(nativeStartReceiving());
  }

  /**
   * Cancels the receiving loop. (See {@link #startReceiving})
   *
   * @throws IOException
   */
  public void cancelReceiving() throws IOException
  {
    executeEvent(new CancelLoopExecutor());
  }

  private void checkForError(int code) throws IOException
  {
    if (code < 0)
    {
      String message = "";
      LocalInterfaceException.DetailedReason detailedReason= LocalInterfaceException.DetailedReason.OTHER;
      switch (code)
      {
        case ERROR_CREATE_FILE:
          message = "The interface could not be opened";
          detailedReason= LocalInterfaceException.DetailedReason.OPEN;
          break;
        case ERROR_GET_TIMEOUTS:
          message = "Error in GetTimeouts";
          detailedReason= LocalInterfaceException.DetailedReason.INIT;
          break;
        case ERROR_SET_COMM_STATE:
          message = "Error in SetCommState";
          detailedReason= LocalInterfaceException.DetailedReason.INIT;          
          break;
        case ERROR_SET_TIMEOUTS:
          message = "Error in SetTimeouts";
          detailedReason= LocalInterfaceException.DetailedReason.INIT;          
          break;
        default:
          message = "Error: " + code;
      }
      throw new LocalInterfaceException(interfaceName, detailedReason, interfaceName + ": " + message);
    }
  }

  /**
   * This method will be called from the receiving loop if data was received.
   *
   * @param data The received bytes.
   */
  public void onDataReceived(byte[] data)
  {
    if (onReceivedHandler != null)
    {
      onReceivedHandler.onDataReceived(data);
    }
  }

  public int onEvent(int eventId)
  {
    int result = 1;

    switch (eventId)
    {
      case 1:
        synchronized (eventLock)
        {

          if (currentEvent != null)
          {
            try
            {
              result = currentEvent.execute();
            }
            catch (IOException ex)
            {
              eventException = ex;
            }
            finally
            {
              currentEvent = null;
            }
            eventLock.notifyAll();
          }
        }
        break;
      case 2:
        synchronized (overlappedSendLock)
        {
          sendFinished = true;
          overlappedSendLock.notifyAll();
        }

    }
    return result;
  }

  public int getBytesInTxBuffer()
  {
    return nativeGetBytesInTxBuffer();
  }

  /**
   * Simple interface to transport received data.
   */
  public interface OnReceivedHandler
  {
    /**
     * This method will be called, if data was received through the COM port.
     *
     * @param data
     */
    public void onDataReceived(byte[] data);

  }

  /**
   * Returns the current data receiver.
   *
   * @return The data receiver
   */
  public OnReceivedHandler getOnReceivedHandler()
  {
    return onReceivedHandler;
  }

  /**
   * Sets the current data receiver.
   * 
   * @param onReceivedHandler The class which implements the OnReceivedHandler interface
   */
  public void setOnReceivedHandler(OnReceivedHandler onReceivedHandler)
  {
    this.onReceivedHandler = onReceivedHandler;
  }

  private final Object eventLock = new Object();
  private EventExecutor currentEvent = null;
  private IOException eventException = null;
  private final Object overlappedSendLock = new Object();
  private boolean sendFinished = false;

  private void executeEvent(EventExecutor event) throws IOException
  {
    synchronized (eventLock)
    {
      try
      {
        currentEvent = event;
        eventException = null;
        checkForError(nativeTrigger());
        while (currentEvent != null)
        {
          eventLock.wait();
        }
        if (eventException != null)
        {
          throw eventException;
        }
      }
      catch (InterruptedException ex)
      {
        throw new InterruptedIOException();
      }
      finally
      {
        currentEvent = null;
        eventException = null;
      }
    }
  }


  /*
   * static constructor.
   */
  static
  {
    if (System.getProperty("os.arch").equalsIgnoreCase("amd64"))
    {
      System.loadLibrary("comportcon64");
    }
    else
    {
      System.loadLibrary("comportcon");
    }
    navtiveInitIDs();
  }

  private abstract class EventExecutor
  {
    public abstract int execute() throws IOException;

  }

  private class SendDataExecutor extends EventExecutor
  {
    private final byte[] data;

    public SendDataExecutor(final byte[] data)
    {
      this.data = data;
    }

    @Override
    public int execute() throws IOException
    {
      nativeSend(data);
      //send(data);
      return 1;
    }

  }

  private class SendOverlappedDataExecutor extends EventExecutor
  {
    private byte[] data;

    public SendOverlappedDataExecutor(byte[] data)
    {
      this.data = data;
    }

    protected SendOverlappedDataExecutor()
    {
      this.data = null;
    }

    @Override
    public int execute() throws IOException
    {
      checkForError(nativeOverlappedSend(data));
      return 1;
    }

    public byte[] getData()
    {
      return data;
    }

    public void setData(final byte[] data)
    {
      this.data = data;
    }

  }

  private class CancelLoopExecutor extends EventExecutor
  {
    @Override
    public int execute() throws IOException
    {
      return 0;
    }

  }

}
