package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.engine.config.OutboundComPortPool;

/**
 * Models a {@link ConnectionTask} that take initative to connect to external devices.
 * They are said to support outbound communication, i.e. communication that
 * is directed from inside the platform to the outside world.
 * An OutboundConnectionTask connects to external devices.
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/09/12
 * Time: 13:25
 */
@ProviderType
public interface OutboundConnectionTask<PCTT extends PartialConnectionTask> extends ConnectionTask<OutboundComPortPool, PCTT>, OutboundConnectionTaskExecutionAspects {

    /**
     * Keeps track of the maximum number of consecutive failures a ConnectionTask can have before marking it as failed.
     *
     * @return the maximum number of consecutive failures this ConnectionTask can have
     */
    public int getMaxNumberOfTries();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundConnectionTask has been tried.
     *
     * @return The current try count
     */
    public int getCurrentTryCount();

    /**
     * Gets the counter that keeps track of the number of times
     * the execution of this OutboundConnectionTask has been retried.
     *
     * @return The current retry count
     *         0 = no retries yet
     *         1 = first retry
     *         ...
     */
    public int getCurrentRetryCount();

    /**
     * Tests if the last execution of this ConnectionTask failed.
     * Note that each time the ConnectionTask executes,
     * this flag will be reset.
     *
     * @return <code>true</code> iff the last execution of this ComTaskExecution failed.
     */
    public boolean lastExecutionFailed ();

    /**
     * Defines the delay before rescheduling this ConnectionTask after a fail
     *
     * @return the time to wait before we may retry after a failing sessions
     */
    public TimeDuration getRescheduleDelay();

}
