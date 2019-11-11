/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import java.time.Clock;

public class ComServerAliveStatusHandler implements TaskExecutor {

    private final Clock clock;
    private final EngineConfigurationService engineConfigurationService;
    private final StatusService statusService;

    public ComServerAliveStatusHandler(Clock clock, EngineConfigurationService engineConfigurationService, StatusService statusService) {
        this.clock = clock;
        this.engineConfigurationService = engineConfigurationService;
        this.statusService = statusService;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        engineConfigurationService.findComServerBySystemName().ifPresent(
                comServer -> {
                    ComServerStatus status = statusService.getStatus();
                    if (status.isBlocked()) {
                        engineConfigurationService.findOrCreateAliveStatus(comServer).update(clock.instant(),
                                engineConfigurationService.getComServerStatusAliveFreq(),
                                status.getBlockTimestamp(), (int) status.getBlockTime().getSeconds());
                    } else {
                        engineConfigurationService.findOrCreateAliveStatus(comServer).update(clock.instant(),
                                engineConfigurationService.getComServerStatusAliveFreq(),
                                null, null);
                    }
                }
        );
    }

}
