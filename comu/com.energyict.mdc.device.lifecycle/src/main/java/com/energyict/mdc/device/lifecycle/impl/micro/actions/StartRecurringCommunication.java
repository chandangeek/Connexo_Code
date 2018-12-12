/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.MicroAction;

import java.time.Instant;
import java.util.List;

public class StartRecurringCommunication extends TranslatableServerMicroAction {

    public StartRecurringCommunication(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ConnectionTask::activate);
        device.getComTaskExecutions().forEach(ComTaskExecution::scheduleNow);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.START_RECURRING_COMMUNICATION;
    }
}