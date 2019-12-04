/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/stacks/ProtocolStack.java $
 * Version:     
 * $Id: ProtocolStack.java 5004 2012-08-15 15:50:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  26.07.2012 13:27:07
 */
package com.elster.protocols.stacks;

import com.elster.protocols.IProtocol;
import com.elster.protocols.IProtocolStateObserver;
import com.elster.protocols.ProtocolState;
import com.elster.protocols.ProtocolStateObservableSupport;
import java.io.IOException;
import java.util.Stack;

/**
 * This class ...
 *
 * @author osse
 */
public class ProtocolStack
{
  private final Stack<IProtocol> protocolLayers;
  private final ProtocolStateObservableSupport stateObservableSupport =
          new ProtocolStateObservableSupport(this);

  public ProtocolStack(final Stack<? extends IProtocol> protocolLayers)
  {
    this.protocolLayers = new Stack<IProtocol>();
    this.protocolLayers.addAll(protocolLayers);


    final IProtocol topLevelProtocol = this.protocolLayers.peek();

    if (topLevelProtocol != null)
    {
      stateObservableSupport.setState(ProtocolState.CLOSE, true);
    }


  }

  public final void close() throws IOException
  {
    try
    {
      stateObservableSupport.setState(ProtocolState.CLOSING, true);
      doClose();
    }
    finally
    {
      cleanup();
      stateObservableSupport.setState(ProtocolState.CLOSE, true);
    }
  }

  protected void doClose() throws IOException
  {
    while (!protocolLayers.isEmpty())
    {
      protocolLayers.pop().close();
    }
  }

  public void cleanup()
  {
    while (!protocolLayers.isEmpty())
    {
      try
      {
        protocolLayers.pop().close();
      }
      catch (Exception ignore)
      {
      }
    }
    stateObservableSupport.setState(ProtocolState.CLOSE, true);
  }

  public void addProtocolStateListener(IProtocolStateObserver observer)
  {
    stateObservableSupport.addProtocolStateListener(observer);
  }

  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    stateObservableSupport.removeProtocolStateListener(observer);
  }

  public synchronized ProtocolState getProtocolState()
  {
    return stateObservableSupport.getProtocolState();
  }

  public synchronized boolean isOpen()
  {
    return stateObservableSupport.isOpen();
  }

}
