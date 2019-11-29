/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/FilterProtocol.java $
 * Version:     
 * $Id: FilterProtocol.java 6465 2013-04-22 14:45:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  04.05.2010 12:01:32
 */
package com.elster.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is the base class for protocols, which changes data of the protocol streams.
 * <P>
 * The default behavior of this class to forward everything to the sub layer.
 *
 * @author osse
 */
public class FilterProtocol extends AbstractProtocol implements IStreamProtocol
{
  protected final IStreamProtocol sublayer;

  /**
   * Creates the filter protocol using the specified protocol as sub layer.
   *
   * @param sublayer
   */
  public FilterProtocol(final IStreamProtocol sublayer)
  {
    this.sublayer = sublayer;

    sublayer.addProtocolStateListener(new IProtocolStateObserver()
    {
      public void openStateChanged(Object sender, ProtocolState oldState, ProtocolState newState)
      {
        sublayerStateChanged(oldState, newState);
      }

      public void connectionBroken(Object sender, Object orign, Exception reason)
      {
        sublayerConnectionBroken(orign, reason);
      }
    });
    setProtocolState(sublayer.getProtocolState());
  }

  /**
   * Called if the state of the sub layer changes.
   * <P>
   * The default implementation simply set the state of this layer to the new state of
   * the sub layer.
   *
   * @param oldState The old state of the sublayer
   * @param newState The new state of the sublayer
   *
   */
  protected void sublayerStateChanged(ProtocolState oldState, ProtocolState newState)
  {
    setProtocolState(newState);
  }

  /**
   * Called if sub layer indicates that the connection was broken.
   * <P>
   * The default implementation simply notifies the observers of this layer.
   */
  protected void sublayerConnectionBroken(Object orign, Exception reason)
  {
    protocolStateSupport.notifyConnectionBroken(orign, reason);
  }

  //@Override
  public InputStream getInputStream()
  {
    return sublayer.getInputStream();
  }

  //@Override
  public OutputStream getOutputStream()
  {
    return sublayer.getOutputStream();
  }

  //@Override
  public void open() throws IOException
  {
    if (!sublayer.isOpen())
    {
      sublayer.open();
    }
  }

  //@Override
  public void close() throws IOException
  {
    sublayer.close();
  }

  @Override
  public void cancelOpen()
  {
    super.cancelOpen();
    sublayer.cancelOpen();
  }

}
