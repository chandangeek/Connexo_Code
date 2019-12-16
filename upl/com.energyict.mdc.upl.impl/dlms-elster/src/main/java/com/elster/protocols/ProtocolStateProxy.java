/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/ProtocolStateProxy.java $
 * Version:     
 * $Id: ProtocolStateProxy.java 5778 2013-01-02 13:52:57Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.08.2012 16:27:19
 */
package com.elster.protocols;

/**
 * Kind of {@link ProtocolStateObservableSupport} that simply reflects the the state of an other {@link IProtocolStateObservable}
 *
 * @author osse
 */
public class ProtocolStateProxy
{
  private final ProtocolStateObservableSupport support;

  public ProtocolStateProxy(final IProtocolStateObservable subObservable,
                            final IProtocolStateObservable sender)
  {

    if (subObservable == null)
    {
      this.support = new ProtocolStateObservableSupport(sender);
    }
    else
    {
      this.support = new ProtocolStateObservableSupport(sender, subObservable.getProtocolState());
      subObservable.addProtocolStateListener(new IProtocolStateObserver()
      {
        //@Override
        public void openStateChanged(final Object sender, final ProtocolState oldState,
                                     final ProtocolState newState)
        {
          support.setState(newState, true);
        }

        //@Override
        public void connectionBroken(final Object sender, final Object orign, final Exception reason)
        {
          support.notifyConnectionBroken(orign, reason);
        }
      });
    }
  }

  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    support.addProtocolStateListener(observer);
  }

  public void removeProtocolStateListener(final IProtocolStateObserver observer)
  {
    support.removeProtocolStateListener(observer);
  }

  public synchronized ProtocolState getProtocolState()
  {
    return support.getProtocolState();
  }

  public synchronized boolean isOpen()
  {
    return support.isOpen();
  }

  public synchronized void checkProtocolState(final ProtocolState expected) throws
          IllegalProtocolStateException
  {
    support.checkProtocolState(expected);
  }

}
