package com.energyict.mdc.device.data;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;

/**
 * Adds behavior to ComTaskExecution that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-09-21 (15:27)
 */
public interface ServerComTaskExecution extends ComTaskExecution {
    // TODO move back to impl package, mdc-all has references on this class though

    /**
     * Sets the given Comport as 'lock'
     * @param comPort the comPort that is about to execute the ComTaskExecution
     */
    public void setLockedComPort(ComPort comPort);

    /**
     * Notifies this ComTaskExecution that the execution has completed without errors.
     * This will recalculate the next execution timestamp
     * and releases the "execution busy" mark,
     * i.e. getExecutingComPort will return <code>null</code>.
     * Note that this does not setup a transactional context
     * and leaves that as the responsibility of the calling code.
     *
     * @see #isExecuting()
     * @see #getExecutingComPort()
     */
    public void executionCompleted ();

    /**
     * Notifies this ComTaskExecution that the execution has failed.
     * This will delay the next execution timestamp
     * and releases the "execution busy" mark,
     * i.e. getExecutingComPort will return <code>null</code>.
     * Note that this does not setup a transactional context
     * and leaves that as the responsibility of the calling code.
     *
     * @see #isExecuting()
     * @see #getExecutingComPort()
     */
    public void executionFailed ();

    /**
     * Tests if the last execution of this ComTaskExecution failed.
     * Note that each time the ComTaskExecution executes,
     * this flag will be reset.
     *
     * @return <code>true</code> iff the last execution of this ComTaskExecution failed.
     */
    public boolean lastExecutionFailed ();

    /**
     * Notifies this ComTaskExecution that execution has been started
     * on the specified OutboundComPort.
     * This will set the last execution timestamp and calculate
     * the next execution timestamp.
     * In addition, this ComTaskExecution will be marked as being
     * currently executed by the OutboundComPort,
     * i.e. getExecutingComPort will return the OutboundComPort
     * Note that this does not setup a transactional context
     * and leaves that as the responsibility of the calling code.
     *
     * @param comPort The OutboundComPort that has picked up this ComTaskExecution for execution
     * @see #isExecuting()
     * @see #getExecutingComPort()
     */
    public void executionStarted (ComPort comPort);

    /**
     * Notifies this ComTaskExecution that the ConnectionTask
     * it was linked to before, was removed (either realy deleted or made obsolete)
     * and will unlink it from that ConnectionTask.
     * If the ConnectionTask happened to be the default one and this ComTaskExecution
     * relies on the default ConnectionTask, then it suffices to flag another
     * ConnectionTask as the default one to link this ComTaskExecution
     * to that new default ConnectionTask.
     */
    public void connectionTaskRemoved ();

    /**
     * Updates this ComTaskExecution with the given ConnectionTask.
     * Regardless whether or not the given ConnectionTask is marked as default
     * (OutboundConnectionTask#isDefault()),
     * this ComTaskExecution wil NOT be marked to use the default
     * (i.e. ComTaskExecution#useDefaultConnectionTask() will return <code>false</code>)
     *
     * @param connectionTask the OutboundConnectionTask which will handle this task if scheduled by the ComServer
     */
    public void updateConnectionTask(ConnectionTask<?,?> connectionTask);

}