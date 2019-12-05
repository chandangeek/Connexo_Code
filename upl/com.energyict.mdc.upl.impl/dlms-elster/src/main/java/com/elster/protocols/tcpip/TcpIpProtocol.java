/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/tcpip/TcpIpProtocol.java $
 * Version:     
 * $Id: TcpIpProtocol.java 4901 2012-07-27 13:22:08Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 24, 2011 5:05:13 PM
 */
package com.elster.protocols.tcpip;

import com.elster.protocols.AbstractProtocol;
import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.ProtocolState;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * TCP/IP Protocol.
 * <P>
 * The input stream is not the original input stream from the socket.(All read methods are returning after 100ms)
 *
 * @author osse
 */
public class TcpIpProtocol extends AbstractProtocol implements IStreamProtocol
{
  private Socket clientSocket;
  private final String dest;
  private final int port;
  private InputStream in;
  private OutputStream out;

  public TcpIpProtocol(final String dest, final int port)
  {
    super();
    this.dest = dest;
    this.port = port;
//    clientSocket= new Socket();
  }

  //@Override
  public void open() throws IOException
  {
    checkAndSetProtocolState(ProtocolState.CLOSE,ProtocolState.OPENING,true);
    try
    {
      clientSocket = new Socket(dest, port);
      clientSocket.setSoTimeout(100);
      clientSocket.setSoLinger(true, 1500);

      in =new TimeoutIgnoringInputStream(clientSocket.getInputStream());
      out = clientSocket.getOutputStream();
    }
    catch (IOException ioException)
    {
      setProtocolState(ProtocolState.CLOSE);
      throw ioException;
    }
    catch (RuntimeException re)
    {
      setProtocolState(ProtocolState.CLOSE);
      throw re;
    }
    setProtocolState(ProtocolState.OPEN);
  }

  //@Override
  public void close() throws IOException
  {
     try
    {
      if (getProtocolState().equals(ProtocolState.OPEN))
      {
        setProtocolState(ProtocolState.CLOSING);
      }

      if (clientSocket!=null && clientSocket.isConnected())
      {
        clientSocket.close();
//        
//        try
//        {
//          out.close();
//          in.close();
//        }
//        finally
//        {
//          clientSocket.close();
//        }
      }
    }
    finally
    {
      clientSocket = null;
      setProtocolState(ProtocolState.CLOSE);
    }
  }

  //@Override
  public InputStream getInputStream()
  {
    if (clientSocket == null)
    {
      throw new IllegalStateException(
              "It is not possible to get the input stream before the protocol is opened");
    }
    return in;
  }

 //@Override
  public OutputStream getOutputStream()
  {
    if (clientSocket == null)
    {
      throw new IllegalStateException(
              "It is not possible to get the output stream before the protocol is opened");
    }
    return out;
  }

  /**
   * Catches timeout exceptions (SocketTimeoutException) and returns a length of 0 instead.
   */
  public static class TimeoutIgnoringInputStream extends FilterInputStream
  {
    public TimeoutIgnoringInputStream(InputStream in)
    {
      super(in);
    }

    @Override
    public int read() throws IOException
    {
      try
      {
        return super.read();
      }
      catch (final SocketTimeoutException sto)
      {
        return -2;
      }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
      try
      {
        return super.read(b);
      }
      catch (final SocketTimeoutException sto)
      {
        return 0;
      }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      try
      {
        return super.read(b, off, len);
      }
      catch (final SocketTimeoutException sto)
      {
        return 0;
      }

    }

    @Override
    public long skip(long n) throws IOException
    {
      try
      {
        return super.skip(n);
      }
      catch (final SocketTimeoutException sto)
      {
        return 0;
      }
    }

  }

}
