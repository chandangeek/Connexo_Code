/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/AbstractProtocol.java $
 * Version:     
 * $Id: AbstractProtocol.java 6726 2013-06-11 13:11:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 19, 2010 2:59:02 PM
 */
package com.elster.protocols;

/**
 * Base class for protocols.<P>
 * It basically manages different protocol states.
 *
 * @author osse
 */
public abstract class AbstractProtocol implements IProtocol
{
  protected final ProtocolStateObservableSupport protocolStateSupport = new ProtocolStateObservableSupport(
          this);
  private volatile boolean cancelOpenFlag = false;

  protected void setProtocolState(final ProtocolState newState, final boolean notify)
  {
    protocolStateSupport.setState(newState, notify);
  }

  protected void checkAndSetProtocolState(final ProtocolState expectedState, final ProtocolState newState,
                                          final boolean notify) throws IllegalProtocolStateException
  {
    protocolStateSupport.checkAndSetProtocolState(expectedState, newState, notify);
  }

  protected void setProtocolState(final ProtocolState newState)
  {
    protocolStateSupport.setState(newState, true);
  }

  protected void fireProtocolStateChange()
  {
    protocolStateSupport.notifyObservers();
  }

  //@Override
  public boolean isOpen()
  {
    return protocolStateSupport.isOpen();
  }

  //@Override
  public ProtocolState getProtocolState()
  {
    return protocolStateSupport.getProtocolState();
  }

  //@Override
  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.addProtocolStateListener(observer);
  }

  //@Override
  public void removeProtocolStateListener(final IProtocolStateObserver observer)
  {
    protocolStateSupport.removeProtocolStateListener(observer);
  }

  protected void checkProtocolState(ProtocolState expected) throws IllegalProtocolStateException
  {
    protocolStateSupport.checkProtocolState(expected);
  }

  //@Override
  public void cancelOpen()
  {
    cancelOpenFlag = true;
  }

  protected final boolean isOpenCanceled()
  {
    return cancelOpenFlag;
  }

  protected void checkOpenCanceled() throws OpenCanceledException
  {
    if (cancelOpenFlag)
    {
      throw new OpenCanceledException();
    }
  }

}
