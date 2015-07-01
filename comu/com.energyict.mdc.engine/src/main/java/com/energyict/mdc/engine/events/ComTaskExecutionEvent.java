package com.energyict.mdc.engine.events;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models an event that relates to the execution of a {@link com.energyict.mdc.tasks.ComTask},
 * aka a ComTaskExecution.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (14:39)
 */
@ProviderType
public interface ComTaskExecutionEvent extends ComServerEvent, ComTaskExecutionRelatedEvent, DeviceRelatedEvent, ConnectionTaskRelatedEvent, ComPortRelatedEvent, ComPortPoolRelatedEvent {

    /**
     * Returns <code>true</code> iff this event indicates
     * that the execution of the related ComTaskExecution started.
     *
     * @return <code>true</code> iff this event indicates the start of the execution of a ComTaskExecution
     */
    public boolean isStart ();

    /**
     * Gets the timestamp on which the execution of the
     * ComTaskExecution was started.
     *
     * @return The timestamp on which the execution started
     */
    public Instant getExecutionStartedTimestamp ();

    /**
     * Returns <code>true</code> iff this event indicates
     * that the execution of the related ComTaskExecution failed.
     *
     * @return <code>true</code> iff this event indicates the failure of the execution of a ComTaskExecution
     */
    public boolean isFailure ();

    /**
     * Returns a message that describes the failure to execute
     * the related ComTaskExecution.
     * This obviously only returns such a message iff this event is an indication
     * for an execution failure, i.e. {@link #isFailure()} return <code>true</code>.
     *
     * @return The message that describes the failure
     */
    public String getFailureMessage ();

    /**
     * Returns <code>true</code> iff this event indicates
     * that the execution of the related ComTaskExecution completed.
     *
     * @return <code>true</code> iff this event indicates the completion of the execution of a ComTaskExecution
     */
    public boolean isCompletion ();

}