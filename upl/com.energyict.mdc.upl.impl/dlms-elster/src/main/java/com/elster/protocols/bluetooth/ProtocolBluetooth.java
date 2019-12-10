/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/bluetooth/ProtocolBluetooth.java $
 * Version:     
 * $Id: ProtocolBluetooth.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 09:33:58
 */
package com.elster.protocols.bluetooth;

import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IBaudrateSupport;
import com.elster.protocols.IStreamProtocol;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * Protocol for using the "Vogts" Bluetooth adapter.
 *
 * @author osse
 */
public class ProtocolBluetooth extends FilterProtocol implements IBaudrateSupport
{
  private final InputStream inputStream;
  private final OutputStream outputStream;
  private int baudrate = 300;

  public ProtocolBluetooth(final IStreamProtocol sublayer)
  {
    super(sublayer);
    inputStream = sublayer.getInputStream();
    outputStream = new EscapeCharOutputStream(sublayer.getOutputStream(), 0xFF);
  }

  @Override
  public void open() throws IOException
  {
    super.open();
    resetAdapter();
    setBaudrate(baudrate);
  }

  private void resetAdapter() throws IOException
  {
      sleep(100);
      sendCommand(0x42);
      sleep(100);
      sendCommand(0x4E);
      sleep(100);
  }
  
  private void sleep(final int millis) throws InterruptedIOException
  {
    try
    {
      Thread.sleep(millis);
    }
    catch (InterruptedException ex)
    {
      throw new InterruptedIOException();
    }
  
  }

  private void sendCommand(final int code) throws IOException
  {
    byte[] codeBytes = new byte[2];
    codeBytes[0] = (byte)0xFF;
    codeBytes[1] = (byte)code;

    sublayer.getOutputStream().write(codeBytes);
    sublayer.getOutputStream().flush();
  }

  @Override
  public InputStream getInputStream()
  {
    return inputStream;
  }

  @Override
  public OutputStream getOutputStream()
  {
    return outputStream;
  }

  //@Override
  public int getBaudrate()
  {
    return baudrate;
  }

  //@Override
  public void setBaudrate(final int newBaudrate) throws IOException
  {
    int linCode = 0;
    switch (newBaudrate)
    {
      case 300:
        linCode = 0x30;
        break;
      case 600:
        linCode = 0x31;
        break;
      case 1200:
        linCode = 0x32;
        break;
      case 2400:
        linCode = 0x33;
        break;
      case 4800:
        linCode = 0x34;
        break;
      case 9600:
        linCode = 0x35;
        break;
      case 19200:
        linCode = 0x36;
        break;
      default:
        throw new IOException("Requested baudrate not supported: " + newBaudrate);
    }
    if (sublayer.isOpen())
    {
      sendCommand(linCode);
      sleep(100);
    }
    this.baudrate = newBaudrate;
  }

}
