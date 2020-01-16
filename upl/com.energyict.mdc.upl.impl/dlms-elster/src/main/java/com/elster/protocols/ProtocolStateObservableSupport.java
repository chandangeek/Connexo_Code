/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/ProtocolStateObservableSupport.java $
 * Version:     
 * $Id: ProtocolStateObservableSupport.java 4901 2012-07-27 13:22:08Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  25.05.2010 12:28:08
 */
package com.elster.protocols;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class helps to implement the IOpenStateObservableInterface.
 *
 * @author osse
 */
public class ProtocolStateObservableSupport implements IProtocolStateObservable
{
  private final Object sender;
  private final List<IProtocolStateObserver> observers = new CopyOnWriteArrayList<IProtocolStateObserver>();
  private ProtocolState notifiedState = ProtocolState.CLOSE;
  private ProtocolState currentState = ProtocolState.CLOSE;

  public ProtocolStateObservableSupport(final Object sender)
  {
    this.sender = sender;
  }

  public ProtocolStateObservableSupport(final Object sender, final ProtocolState initialState)
  {
    this.sender = sender;
    this.notifiedState = initialState;
    this.currentState = initialState;
  }

  //@Override
  public void addProtocolStateListener(final IProtocolStateObserver observer)
  {
    if (!observers.contains(observer))
    {
      observers.add(observer);
    }
  }

  //@Override
  public void removeProtocolStateListener(IProtocolStateObserver observer)
  {
    if (observers.contains(observer))
    {
      observers.remove(observer);
    }
  }

  protected void notifyObservers(final ProtocolState oldState, final ProtocolState newState)
  {

    for (IProtocolStateObserver l : observers)
    {
      l.openStateChanged(sender, oldState, newState);
    }
  }

  public void notifyConnectionBroken(Object origin, Exception reason)
  {
    for (IProtocolStateObserver l : observers)
    {
      l.connectionBroken(sender, origin, reason);
    }
  }

  /**
   * Notifies the registered observers.
   */
  public void notifyObservers()
  {
    ProtocolState localOldState;
    ProtocolState localNewState;

    synchronized (this)
    {
      localOldState = notifiedState;
      localNewState = currentState;
      notifiedState = currentState;
    }

    if (localOldState != localNewState)
    {
      notifyObservers(localOldState, localNewState);
    }
  }

  /**
   * Sets the (new) state.
   *
   * @param newState the new state.
   * @param notify If {@code true} the observers will be notified automatically, otherwise {@link #notifyObservers() must be called}
   */
  public void setState(ProtocolState newState, boolean notify)
  {
    if (notify)
    {
      ProtocolState localOldState;
      ProtocolState localNewState;

      synchronized (this)
      {
        this.currentState = newState;
        localOldState = notifiedState;
        localNewState = newState;
        notifiedState = newState;
      }

      if (localOldState != localNewState)
      {
        notifyObservers(localOldState, localNewState);
      }
    }
    else
    {
      synchronized (this)
      {
        this.currentState = newState;
      }
    }
  }

  /**
   * Sets the new state if current state equals the expected state.
   * <P>
   * This method makes synchronization easier.
   *
   * @param expectedState The expected current state.
   * @param newState the new state.
   * @param notify If {@code true} the observers will be notified automatically, otherwise {@link #notifyObservers() must be called}
   * @return Returns true if the expected state was the state of the protocol.
   */
  public boolean setState(final ProtocolState expectedState, final ProtocolState newState,
                          final boolean notify)
  {
    if (notify)
    {
      ProtocolState localOldState;
      ProtocolState localNewState;

      synchronized (this)
      {
        if (expectedState != this.currentState)
        {
          return false;
        }

        this.currentState = newState;
        localOldState = notifiedState;
        localNewState = newState;
        notifiedState = newState;
      }

      if (localOldState != localNewState)
      {
        notifyObservers(localOldState, localNewState);
      }
    }
    else
    {
      synchronized (this)
      {
        if (expectedState != this.currentState)
        {
          return false;
        }
        this.currentState = newState;
      }
    }
    return true;
  }

  public synchronized ProtocolState getProtocolState()
  {
    return currentState;
  }

  public synchronized boolean isOpen()
  {
    return currentState == ProtocolState.OPEN;
  }

  public synchronized void checkProtocolState(ProtocolState expected) throws IllegalProtocolStateException
  {
    if (!currentState.equals(expected))
    {
      throw new IllegalProtocolStateException(expected, currentState);
    }
  }

  public void checkAndSetProtocolState(final ProtocolState expectedState,
                                          final ProtocolState newState,
                                          final boolean notify) throws
          IllegalProtocolStateException
  {
    synchronized (this)
    {
      if (!setState(expectedState, newState, false))
      {
        throw new IllegalProtocolStateException(expectedState, this.currentState);
      }
    }

    if (notify)
    {
      notifyObservers();
    }
  }

}
