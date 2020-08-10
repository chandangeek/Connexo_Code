/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.xml.bind.annotation.XmlAttribute;
import java.time.Clock;
import java.util.List;

public abstract class OutboundConnectionTaskImpl<PCTT extends PartialOutboundConnectionTask>
        extends ConnectionTaskImpl<PCTT, OutboundComPortPool>
        implements OutboundConnectionTask<PCTT> {

    /**
     * The Default amount of seconds a ComTask should wait before retrying.
     */
    public static final int DEFAULT_COMTASK_FAILURE_RESCHEDULE_DELAY_SECONDS = 300;
    protected static final int DEFAULT_MAX_NUMBER_OF_TRIES = 3;

    private int currentRetryCount;
    private int currentTryCount;
    private boolean lastExecutionFailed;
    private TimeDuration rescheduleDelay;

    protected OutboundConnectionTaskImpl() {
        super();
    }

    protected OutboundConnectionTaskImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ServerConnectionTaskService connectionTaskService, ServerCommunicationTaskService communicationTaskService, ProtocolPluggableService protocolPluggableService) {
        super(dataModel, eventService, thesaurus, clock, connectionTaskService, communicationTaskService, protocolPluggableService);
    }

    @Override
    protected void doExecutionStarted(ComPort comPort, List<String> updatedColumns) {
        super.doExecutionStarted(comPort, updatedColumns);
        this.lastExecutionFailed = false;
        updatedColumns.add(ConnectionTaskFields.LAST_EXECUTION_FAILED.fieldName());
    }

    @Override
    protected void doExecutionCompleted(List<String> updatedFields) {
        super.doExecutionCompleted(updatedFields);
        resetCurrentRetryCount();
        updatedFields.add(ConnectionTaskFields.CURRENT_RETRY_COUNT.fieldName());
    }

    @Override
    public void executionFailed() {
        this.doNotTouchParentDevice();
        this.setExecutingComPort(null);
        this.incrementCurrentRetryCount();
        if (doWeNeedToRetryTheConnectionTask()) {
            this.doExecutionAttemptFailed();
        } else {
            this.doExecutionFailed();
        }
        this.update(ConnectionTaskFields.COM_PORT.fieldName(), ConnectionTaskFields.CURRENT_RETRY_COUNT.fieldName(), ConnectionTaskFields.LAST_EXECUTION_FAILED.fieldName());
    }

    @Override
    public void executionRescheduled() {
        doExecutionRescheduled();
        update(ConnectionTaskFields.COM_PORT.fieldName());
    }

    protected void doExecutionRescheduled() {
        setExecutingComPort(null);
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
    @XmlAttribute
    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    @Override
    @XmlAttribute
    public int getCurrentTryCount() {
        currentTryCount = getCurrentRetryCount() + 1;
        return currentTryCount;
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
        } else {
            return this.getRescheduleDelay();
        }
    }

    @Override
    @XmlAttribute
    public TimeDuration getRescheduleDelay() {
        if (this.getPartialConnectionTask() != null) {
            rescheduleDelay = this.getPartialConnectionTask().getRescheduleDelay();
        }
        return rescheduleDelay;
    }

}
