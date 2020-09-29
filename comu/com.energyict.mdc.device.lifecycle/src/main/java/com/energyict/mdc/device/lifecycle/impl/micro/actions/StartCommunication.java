/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.MicroAction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will start the  communication with the device
 * by activating all connection schedule all communication tasks
 * to execute now.
 * @see {@link MicroAction#START_COMMUNICATION}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (14:32)
 */
public class StartCommunication extends TranslatableServerMicroAction {

    public StartCommunication(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public void execute(Device device, Instant effectiveTimestamp, List<ExecutableActionProperty> properties) {
        device.getConnectionTasks().forEach(ConnectionTask::activate);
        device.getComTaskExecutions().forEach(ComTaskExecution::scheduleNow);
    }

    @Override
    protected MicroAction getMicroAction() {
        return MicroAction.START_COMMUNICATION;
    }
}