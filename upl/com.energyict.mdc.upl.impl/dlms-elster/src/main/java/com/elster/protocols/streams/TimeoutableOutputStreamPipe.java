/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/TimeoutableOutputStreamPipe.java $
 * Version:     
 * $Id: TimeoutableOutputStreamPipe.java 2649 2011-02-08 13:52:13Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  29.04.2010 08:40:12
 */

package com.elster.protocols.streams;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is the output stream side of an pipe which supports timeouts.
 * <P>
 * See {@link TimeoutableInputStreamPipe }
 *
 * @author osse
 */
public class TimeoutableOutputStreamPipe extends OutputStream
{
  IByteSink receiver;

  /**
   * Creates the pipe.
   *
   * @param receiver The input stream side of the pipe.
   */
  public TimeoutableOutputStreamPipe(IByteSink receiver)
  {
    this.receiver = receiver;
  }

  /**
   * Create this side of the pipe.<P>
   * The input stream side must be connected with {@link #connect(com.elster.protocols.streams.TimeoutableInputStreamPipe) }
   * before the write methods can be called.
   *
   */
  public TimeoutableOutputStreamPipe()
  {
    this(null);
  }

  /**
   * Connects this side of the pipe with the input stream side.
   *
   */
  public void connect(TimeoutableInputStreamPipe timeoutableInputStream)
  {
    this.receiver= timeoutableInputStream;
  }


  @Override
  public void write(int b) throws IOException
  {
    receiver.put((byte) b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException
  {
    receiver.put(b,off,len);
  }

  @Override
  public void write(byte[] b) throws IOException
  {
    receiver.put(b);
  }

  @Override
  public void close() throws IOException
  {
    super.close();
    if (receiver!=null)
    {
      receiver.finishPut();
    }
  }

  @Override
  public void flush() throws IOException
  {
    receiver.flush();
  }

}
