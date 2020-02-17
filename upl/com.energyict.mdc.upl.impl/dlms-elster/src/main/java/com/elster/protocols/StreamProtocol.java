/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/StreamProtocol.java $
 * Version:     
 * $Id: StreamProtocol.java 4874 2012-07-19 15:23:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.05.2010 10:48:08
 */

package com.elster.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is a simple Protocol for 2 given streams.
 *
 * @author osse
 */
public class StreamProtocol  extends AbstractProtocol implements IStreamProtocol
{
  private final InputStream in;
  private final OutputStream out;
  private final boolean closeStreamsOnClose;

  /**
   * Constructor.
   *
   * @param in The input stream of the protocol
   * @param out The output stream of the protocol
   * @param closeStreamsOnClose If {@code true} the streams will be closed by the method {#close}.
   */
  public StreamProtocol(InputStream in, OutputStream out, boolean closeStreamsOnClose)
  {
    this.in = in;
    this.out = out;
    this.closeStreamsOnClose = closeStreamsOnClose;
  }



  //@Override
  public InputStream getInputStream()
  {
    return in;
  }

  //@Override
  public OutputStream getOutputStream()
  {
    return out;
  }


  //@Override
  public void close() throws IOException
  {
    if (closeStreamsOnClose)
    {
      in.close();
      out.close();
    }
    setProtocolState(ProtocolState.CLOSE);
  }

  public void open() throws IOException
  {
    setProtocolState(ProtocolState.OPEN);
  }

}
