/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.HasLastComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComPort;

import java.time.Instant;

/**
 * Adds behavior to ComTaskExecution that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-09-21 (15:27)
 */
public interface ServerComTaskExecution extends ComTaskExecution, HasLastComTaskExecutionSession {

    /**
     * Unlocks this ComTaskExecution, basically undoing the effect
     * of the attemptLock method providing that that was successful.
     */
    public void unlock();

    /**
     * Sets the given Comport as 'lock'
     *
     * @param comPort the comPort that is about to execute the ComTaskExecution
     */
    void setLockedComPort(ComPort comPort);

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
    void executionCompleted();

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
    void executionFailed();

    /**
     * Notifies this ComTaskExecution that it should be rescheduled based on the given date
     *
     * @param rescheduleDate the given reschedule date (additional restrictions can be applicable)
     */
    void executionRescheduled(Instant rescheduleDate);

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
    void executionStarted(ComPort comPort);

    /**
     * Makes this ComTaskExecution obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceService} finder methods.
     * <p>
     * Note: the call needs to run in a Transaction, no additional save() is required.
     */
    void makeObsolete();

    /**
     * Notifies this ComTaskExecution that the ConnectionTask
     * it was linked to before, was removed (either realy deleted or made obsolete)
     * and will unlink it from that ConnectionTask.
     * If the ConnectionTask happened to be the default one and this ComTaskExecution
     * relies on the default ConnectionTask, then it suffices to flag another
     * ConnectionTask as the default one to link this ComTaskExecution
     * to that new default ConnectionTask.
     */
    void connectionTaskRemoved();

}