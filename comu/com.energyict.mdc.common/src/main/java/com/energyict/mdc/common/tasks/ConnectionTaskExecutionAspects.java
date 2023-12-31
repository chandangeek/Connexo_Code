/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.common.comserver.ComPort;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.mdc.protocol.journal.ProtocolLoggingSupport;

/**
 * Todo:
 * Used to add behavior to {@link ConnectionTask} that is private
 * to the server side implementation but will need to be refactored
 * to a wrapper entity in the mdc.engine bundle.
 * The ComServer attribute of a ConnectionTask will move to that wrapper entity
 * and implement the behavior that was on ConnectionTask before that relates to execution.
 * Current validation constraints on ConnectionTaskImpl e.g. that prohibit a ConnectionTask
 * from being obsoleted when it is being executed will obviously need to be moved too.
 *
 * @author sva
 * @since 2012-09-27 (13:01)
 */
@ConsumerType
public interface ConnectionTaskExecutionAspects extends ProtocolLoggingSupport {

    /**
     * Notifies this ConnectionTask that execution has been started.
     * This will update the last communication started timestamp.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     *
     * @param comPort The ComServer that is started the execution
     */
    void executionStarted(ComPort comPort);

    /**
     * Notifies this ConnectionTask that the execution has completed without errors.
     * This will update the last communication successful end timestamp.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     *
     */
    void executionCompleted();

    /**
     * Notifies this ConnectionTask that the execution has failed.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     */
    void executionFailed();

    /**
     * Notifies this ConnectionTask that the execution needs to be rescheduled.
     * Not that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     */
    public void executionRescheduled();

}