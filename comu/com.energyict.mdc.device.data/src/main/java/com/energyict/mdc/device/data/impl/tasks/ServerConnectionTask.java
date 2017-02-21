/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;

/**
 * Adds behavior to {@link ConnectionTask} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-07-16 (13:38)
 */
public interface ServerConnectionTask<CPPT extends ComPortPool, PCTT extends PartialConnectionTask> extends ConnectionTask<CPPT, PCTT> {

    /**
     * Makes this ConnectionTask obsolete, i.e. it will no longer execute
     * nor will it be returned by {@link DeviceService} finder methods.
     */
    void makeObsolete();

    /**
     * Revalidates the properties of this ConnectionTask and will
     * set the {@link ConnectionTaskLifecycleStatus} to
     * {@link ConnectionTaskLifecycleStatus#INCOMPLETE}
     * if at least one required property is missing.
     * Assumes that no changes have been applied to
     * the ConnectionTask and intended to be called
     * when required properties were removed
     * on the {@link PartialConnectionTask}.
     */
    void revalidatePropertiesAndAdjustStatus();

    /**
     * Notifies this ConnectionTask that it is about to be deleted
     * as part of the delete of the {@link com.energyict.mdc.device.data.Device}.
     */
    void notifyDelete ();

    /**
     * Notifies this OutboundConnectionTask that one of its
     * {@link ComTaskExecution}s was rescheduled.
     *
     * @param comTask The ScheduledComTask that was rescheduled
     */
    void scheduledComTaskRescheduled(ComTaskExecution comTask);

    /**
     * Notifies this OutboundConnectionTask that the priority
     * of one of its {@link ComTaskExecution}s changed.
     *
     * @param comTask The ScheduledComTask whose priority changed
     */
    void scheduledComTaskChangedPriority(ComTaskExecution comTask);

}