/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncServiceInvocation.java $
 * Version:     
 * $Id: CosemAsyncServiceInvocation.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  11.05.2010 14:12:23
 */
package com.elster.dlms.cosem.applicationlayer;

//import com.elster.dlms.apdu.axdr.doc.AXdrDocBase;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Abstract base class for asynchronous COSEM service invocations.<p>
 * Subclasses are used by the {@link CosemAsyncApplicationLayer }
 *
 * @author osse
 */
public abstract class CosemAsyncServiceInvocation
{
  private final ArrayList<InvocationListener> listeners = new ArrayList<InvocationListener>(1);

  public interface InvocationListener
  {
    void stateChanged(CosemAsyncServiceInvocation sender, State oldState, State newState);
    void invocationFinished(CosemAsyncServiceInvocation sender, State newState);
  }

  public enum ServiceType
  {
    GET, SET, ACTION, TASK
  }

  public enum State
  {
    ENQUEUED, EXECUTING, FINISHED, ERROR, CANCELED
  };

  private static final EnumSet<State> FINAL_STATES = EnumSet.of(State.CANCELED, State.FINISHED, State.ERROR);
  private State state;
  private IOException errorReason;

  public abstract ServiceType getServiceType();

  public State getState()
  {
    return state;
  }

  public IOException getErrorReason()
  {
    return errorReason;
  }

  private final InvocationListener[] EMPTY_LISTNERS = new InvocationListener[0];

  void setState(final State newState)
  {
    setState(null, newState, null);
  }

  boolean setState(final State expectedState,final State newState)
  {
    return setState(expectedState, newState, null);
  }

  /**
   * Sets the error reason to the given reason.<P>
   * The state will be set to ERROR.
   *
   * @param reason The reason.
   */
  void setError(IOException reason)
  {
    setState(null, State.ERROR, reason);
  }

  private boolean setState(State expectedState, State newState, IOException reason)
  {
    State oldState;
    InvocationListener[] localListeners = null;

    synchronized (this)
    {
      if (expectedState!=null && expectedState != state)
      {
        return false;
      }

      errorReason= reason;

      oldState = this.state;
      if (oldState != newState)
      {
        this.state = newState;
        notifyAll();
      }
      localListeners = listeners.toArray(EMPTY_LISTNERS);

      if (FINAL_STATES.contains(newState))
      {
        listeners.clear();
        listeners.trimToSize();
      }
    }

    if (oldState != newState)
    {
      for (InvocationListener l : localListeners)
      {
        l.stateChanged(this, oldState, newState);
      }

      if (FINAL_STATES.contains(newState))
      {
        for (InvocationListener l : localListeners)
        {
          l.invocationFinished(this, newState);
        }
      }
    }

    return true;
  }

  /**
   * Cancels the request.<P>
   * It is not guaranteed that the request will be really canceled.
   *
   */
  public boolean cancel()
  {
    return setState(State.ENQUEUED, State.CANCELED);
  }


  /**
   * Waits until the async service invocation reaches a final state.
   *
   * @throws InterruptedException
   */
  public void waitFor() throws InterruptedIOException
  {
    synchronized (this)
    {
      while(!FINAL_STATES.contains(getState()))
      {
        try
        {
          wait();
        }
        catch (InterruptedException ex)
        {
          throw new InterruptedIOException(ex.getMessage());
        }
      }
    }
  }

  /**
   * Adds the given listener to this invocation object.<P>
   * The listener will be automatically removed if this object reaches a final state.<br>
   * If a final state is already reached {@link CosemAsyncServiceInvocation.InvocationListener#invocationFinished(com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation, com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation.State)}
   * will be called directly and the listener will not be added.<P>
   * (The listener can also be removed by calling {@link #removeListener(com.elster.dlms.cosem.applicationlayer.CosemAsyncServiceInvocation.InvocationListener)}.)
   *
   * @param listener
   */
  public void addListener(final InvocationListener listener)
  {
    boolean finalState;
    State currentState;

    synchronized (this)
    {
      currentState=state;
      finalState = FINAL_STATES.contains(currentState);
      if (!finalState)
      {
        listeners.add(listener);
      }
    }

    if (finalState)
    {
      listener.invocationFinished(this, currentState);
    }
  }

  /**
   * Removes the specified listener.
   *
   * @param listener The listener to remove.
   */
  public synchronized void removeListener(final InvocationListener listener)
  {
    listeners.remove(listener);
  }


}
