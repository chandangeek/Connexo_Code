package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.engine.config.ComServer;

import aQute.bnd.annotation.ProviderType;

import java.sql.SQLException;

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
@ProviderType
public interface ConnectionTaskExecutionAspects {

    /**
     * Notifies this ConnectionTask that execution has been started.
     * This will update the last communication started timestamp.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     *
     * @param comServer The ComServer that is started the execution
     * @throws SQLException
     */
    public void executionStarted (ComServer comServer);

    /**
     * Notifies this ConnectionTask that the execution has completed without errors.
     * This will update the last communication successful end timestamp.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     *
     * @throws SQLException
     */
    public void executionCompleted ();

    /**
     * Notifies this ConnectionTask that the execution has failed.
     * Note that this requires that there is already a transactional
     * context active, i.e. no attempt will be made to create one.
     *
     * @throws SQLException
     */
    public void executionFailed ();

    /**
     * Notifies this OutboundConnectionTask that one of its
     * {@link ComTaskExecution}s was rescheduled.
     *
     * @param comTask The ScheduledComTask that was rescheduled
     */
    public void scheduledComTaskRescheduled (ComTaskExecution comTask);

    /**
     * Notifies this OutboundConnectionTask that the priority
     * of one of its {@link ComTaskExecution}s changed.
     *
     * @param comTask The ScheduledComTask whose priority changed
     */
    public void scheduledComTaskChangedPriority (ComTaskExecution comTask);

}