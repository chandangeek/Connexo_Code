package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import java.time.Clock;
import java.util.List;

import com.elster.jupiter.time.TimeDuration;

import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

/**
 * Provides an abstract implementation for the {@link OutboundConnectionTask} interface,
 * leaving a number of details to the expected subclasses that will deal
 * with scheduling and initiation.
 * <p>
 * Copyrights EnergyICT
 * Date: 13/09/12
 * Time: 13:10
 */
public abstract class OutboundConnectionTaskImpl<PCTT extends PartialOutboundConnectionTask>
        extends ConnectionTaskImpl<PCTT, OutboundComPortPool>
        implements OutboundConnectionTask<PCTT> {

    /**
     * The Default amount of seconds a ComTask should wait before retrying.
     */
    public static final int DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS = 300;
    protected static final int DEFAULT_MAX_NUMBER_OF_TRIES = 3;

    private int currentRetryCount;
    private boolean lastExecutionFailed;

    protected OutboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, DeviceService deviceService, ProtocolPluggableService protocolPluggableService, RelationService relationService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, deviceService, protocolPluggableService, relationService);
    }

    @Override
    protected void doExecutionStarted(ComServer comServer, List<String> updatedColumns) {
        super.doExecutionStarted(comServer, updatedColumns);
        this.lastExecutionFailed = false;
        updatedColumns.add(ConnectionTaskFields.LAST_EXECUTION_FAILED.fieldName());
    }

    @Override
    protected void doExecutionCompleted() {
        super.doExecutionCompleted();
        resetCurrentRetryCount();
    }

    @Override
    public void executionFailed() {
        this.setExecutingComServer(null);
        this.incrementCurrentRetryCount();
        if (doWeNeedToRetryTheConnectionTask()) {
            this.doExecutionAttemptFailed();
        } else {
            this.doExecutionFailed();
        }
        this.update();
    }

    protected boolean doWeNeedToRetryTheConnectionTask() {
        return currentRetryCount < this.getMaxNumberOfTries();
    }

    protected void doExecutionAttemptFailed() {
        this.lastExecutionFailed = true;
    }

    protected void doExecutionFailed() {
        this.lastExecutionFailed = true;
    }

    @Override
    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    @Override
    public int getCurrentTryCount() {
        return getCurrentRetryCount() + 1;
    }

    @Override
    public boolean lastExecutionFailed() {
        return lastExecutionFailed;
    }

    protected void incrementCurrentRetryCount() {
        this.currentRetryCount++;
    }

    protected void resetCurrentRetryCount() {
        this.currentRetryCount = 0;
    }

    /**
     * The rescheduleRetryDelay used to be fetched as follow:
     * <ol>
     * <li>Check if this {@link OutboundConnectionTask} has a proper {@link OutboundConnectionTask#getRescheduleDelay()}</li>
     * <li>Check the System Parameter COMTASK_FAILURE_RESCHEDULE_DELAY</li>
     * <li>When none of the above are provided, we return the default value of the above System Parameter (300 seconds at the time)</li>
     * </ol>
     * However, the jupiter platform does not yet have the concept of system parameters so we can only do 1 and 3.
     *
     * @return the configured rescheduleRetryDelay
     */
    protected TimeDuration getRescheduleRetryDelay() {
        if (this.getRescheduleDelay() == null || getRescheduleDelay().getSeconds() <= 0) {
            return new TimeDuration(DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS, TimeDuration.TimeUnit.SECONDS);
        }
        else {
            return this.getRescheduleDelay();
        }
    }

    @Override
    public TimeDuration getRescheduleDelay() {
        return this.getPartialConnectionTask().getRescheduleDelay();
    }

}
