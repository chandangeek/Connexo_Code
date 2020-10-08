/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;

import java.time.Instant;
import java.util.List;

public class StartRecurringCommunication extends TranslatableServerMicroAction {

    public StartRecurringCommunication(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ConnectionTask::activate);
        device.getComTaskExecutions().stream()
                .forEach(comTaskExecution -> {
                    if (comTaskExecution.getPlannedNextExecutionTimestamp() != null) {
                        comTaskExecution.scheduleNow();
                    }
                });
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.START_RECURRING_COMMUNICATION;
    }
}