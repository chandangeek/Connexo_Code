/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemExecutor.java $
 * Version:     
 * $Id: CosemExecutor.java 3913 2012-01-16 12:54:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 4, 2010 4:48:05 PM
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for executable COSEM methods and processes.
 *
 * @author osse
 */
public abstract class CosemExecutor
{
  /**
   * Interface for listeners which are interested in changes of the execution state.
   */
  public interface IExecutionStateChangedListener
  {
    void executionStateChanged(Object sender, ExecutionState oldState, ExecutionState newState);
  }
  
  
  private Exception errorReason;
  
  
  

  /**
   * Execution state for executable methods and processes.
   */
  public enum ExecutionState
  {
    /**
     * The method or process was not executed.
     */
    NONE,
    /**
     * The method or process is executed at the moment.
     */
    RUNNING,
    /**
     * The method or process was successfully executed.
     */
    FINISHED,
    /**
     * An error occurred during execution of the method or process.
     */
    ERROR,
    /**
     * The execution was canceled.
     */
    CANCELED
  };

  private ExecutionState executionState = ExecutionState.NONE;

  /**
   * Sole constructor.
   */
  public CosemExecutor()
  {
  }

  /**
   * Returns the execution state of this method or process.
   *
   * @return
   */
  public ExecutionState getExecutionState()
  {
    return executionState;
  }

  /**
   * Sets the execution state of this method or process.<P>
   * Fire state change event as necessary.
   *
   * @param executionState The new execution state.
   */
  protected void setExecutionState(final ExecutionState executionState)
  {
    if (this.executionState != executionState)
    {
      final ExecutionState old = this.executionState;
      this.executionState = executionState;
      fireExecutionStateChanged(old, executionState);
    }
  }

  private final List<IExecutionStateChangedListener> listener =
          new LinkedList<IExecutionStateChangedListener>();

  /**
   * Adds an listener to this object.
   *
   * @param changeListener The listener to add.
   */
  public void addListener(final IExecutionStateChangedListener changeListener)
  {
    if (changeListener == null)
    {
      throw new IllegalArgumentException("changeListener must not be null");
    }
    synchronized (listener)
    {
      listener.add(changeListener);
    }
  }

  /**
   * Removes the listener from this object.
   *
   * @param changeListener The listener to remove.
   */
  public void removeListener(final IExecutionStateChangedListener changeListener)
  {
    if (changeListener == null)
    {
      throw new IllegalArgumentException("changeListener must not be null");      
    }

    synchronized (listener)
    {
      listener.remove(changeListener);
    }
  }

  private final static IExecutionStateChangedListener[] EMPTY_LISTENERS =
          new IExecutionStateChangedListener[0];

  private void fireExecutionStateChanged(final ExecutionState oldState,final ExecutionState newState)
  {
    IExecutionStateChangedListener[] localListeners;
    synchronized (listener)
    {
      if (listener.isEmpty())
      {
        return;
      }
      localListeners = listener.toArray(EMPTY_LISTENERS);
    }

    for (IExecutionStateChangedListener l : localListeners)
    {
      l.executionStateChanged(this, oldState, newState);
    }
  }

  public Exception getErrorReason()
  {
    return errorReason;
  }

  public void setErrorReason(final Exception errorReason)
  {
    this.errorReason = errorReason;
  }
  
  
  public boolean isExecutable(final CosemApplicationLayer applicationLayer)
  {
    return true;
  }
  
  
  
  

}
