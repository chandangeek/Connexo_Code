/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcInformationBlockIn.java $
 * Version:     
 * $Id: HdlcInformationBlockIn.java 3843 2011-12-12 16:55:48Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  11.05.2010 09:43:49
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.streams.TimeoutableInputStreamPipe;
import com.elster.protocols.streams.TimeoutableOutputStreamPipe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents one HDLC information block for receiving.
 *
 * @author osse
 */
public class HdlcInformationBlockIn
{
  long size()
  {
    return inputStream.available();
  }

  public enum State
  {
    NONE, RECEIVING, FINISHED, ERROR
  };

  private final TimeoutableInputStreamPipe inputStream;
  private final TimeoutableOutputStreamPipe outputStream;
  private State state;

  public HdlcInformationBlockIn()
  {
    super();
    inputStream = new TimeoutableInputStreamPipe(0, 0);
    outputStream = new TimeoutableOutputStreamPipe(inputStream);
  }

  OutputStream getOutputStream()
  {
    return outputStream;
  }

  public InputStream getInputStream()
  {
    return inputStream;
  }

  /**
   * Returns the bytes of this information block.<P>
   * This method blocks until the information block was finished or an
   * error occurred.
   *
   * @deprecated This method needs to be revised (or to be deleted). It internally uses getInputStream() which leads to side effects:
   *  This method returns the data only one time. Simultaneous use of getInputStream() will lead to unexpected behaviour.
   * @return The received bytes.
   * @throws IOException
   */
  @Deprecated
  public byte[] getBytes() throws IOException
  {
    byte[] buf = new byte[128];

    ByteArrayOutputStream out = new ByteArrayOutputStream(getInputStream().available());

    int oneByte = getInputStream().read();

    while (oneByte >= 0)
    {
      out.write(oneByte);

      int a = getInputStream().available();
      while (a > 0)
      {
        int s = Math.min(a, buf.length);
        int bytesRead = getInputStream().read(buf, 0, s);
        out.write(buf, 0, bytesRead);
        a = getInputStream().available();
      }
      oneByte = getInputStream().read();
    }

    return out.toByteArray();
  }

  /**
   * Returns the state of this information block.
   *
   * @return The state.
   */
  public State getState()
  {
    return state;
  }

  void finishBlock()
  {
    try
    {
      outputStream.close();
    }
    catch (IOException ex)
    {
      Logger.getLogger(HdlcInformationBlockIn.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
