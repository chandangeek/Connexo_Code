/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/modem/ModemProtocol.java $
 * Version:     
 * $Id: ModemProtocol.java 6465 2013-04-22 14:45:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 5, 2010 9:20:13 AM
 */
package com.elster.protocols.modem;

import com.elster.protocols.FilterProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.streams.TimeoutInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Simple protocol to establish a connection via modem.
 *
 * @author osse
 */
public class ModemProtocol extends FilterProtocol
{
  private final OutputStream out;
  private final TimeoutInputStream in;
  private String modemInit = "AT&F";
  private String number = "0w";

  public ModemProtocol(final IStreamProtocol sublayer)
  {
    super(sublayer);
    in = new TimeoutInputStream(sublayer.getInputStream());
    out = sublayer.getOutputStream();
    // reader = new InputStreamReader(in);
  }

  private boolean setTimeout(final int timeout)
  {
    in.setTimeout(timeout);
    return true;
  }

  @Override
  public void open() throws IOException
  {
    setProtocolState(ProtocolState.OPENING);
    try
    {
      try
      {
        setTimeout(3000);
        sendLine("AT");
        Thread.sleep(2000);
        in.skip(in.available());
        sendCommand(modemInit);
        String dialString = "ATD" + number;
        sendLine(dialString);
        String dialAnswer;
        do
        {
          setTimeout(180 * 1000);
          dialAnswer = readLine();

          if (dialAnswer.equals(dialString)) //echo
          {
            dialAnswer = "";
          }
        }
        while (dialAnswer.length() == 0);

        if (!dialAnswer.toUpperCase(Locale.US).contains("CONNECT"))
        {
          throw new IOException("CONNECT String not received: " + dialAnswer);
        }
        setProtocolState(ProtocolState.OPEN);
      }
      catch (InterruptedException ex)
      {
        throw new InterruptedIOException();
      }
    }
    finally
    {
      if (!isOpen())
      {
        try
        {
          super.close();
        }
        finally
        {
          setProtocolState(ProtocolState.CLOSE);
        }
      }
    }
  }

  private void sendLine(final String line) throws IOException
  {
    out.write(line.getBytes());
    out.write("\r\n".getBytes());
    out.flush();
  }

  private void sendCommand(final String command) throws IOException
  {
    sendLine(command);
    String line = readLine();

    if (line.equals(command)) //ignore echo.
    {
      line = readLine();
    }

    int emptyLineCount = 0;
    while (line.length()==0) //isEmpty() not available in java 1.5
    {
      emptyLineCount++;
      if (emptyLineCount > 5)
      {
        throw new IOException("Too many empty lines received while waiting for OK.");
      }



      line = readLine();
    }


    if (!"OK".equals(line.toUpperCase(Locale.US)))
    {
      throw new IOException("No OK received.");
    }

  }

  private String readLine() throws IOException
  {
    final StringBuilder line = new StringBuilder();
    int c;
    do
    {
      c = in.read();

      if (c != 10 && c != 13)
      {
        line.append((char)c);
      }

      if (c < 0)
      {
        throw new EOFException();
      }
      
      checkOpenCanceled();
    }
    while (c != 10);
    return line.toString();
  }

  public String getModemInit()
  {
    return modemInit;
  }

  public void setModemInit(final String modemInit)
  {
    this.modemInit = modemInit;
  }

  public String getNumber()
  {
    return number;
  }

  public void setNumber(final String number)
  {
    this.number = number;
  }

  @Override
  protected void sublayerStateChanged(final ProtocolState oldState, final ProtocolState newState)
  {
    switch (newState)
    {
      case CLOSING:
        if (getProtocolState() == ProtocolState.OPEN || getProtocolState() == ProtocolState.OPENING)
        {
          setProtocolState(ProtocolState.CLOSING);
        }
        break;
      case CLOSE:
        setProtocolState(ProtocolState.CLOSE);
        break;
    }
  }

}
