/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.engine.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import java.time.Clock;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComServerAliveLoopImpl implements Runnable {

    private final ScheduledThreadPoolExecutor executor;
    private final Clock clock;
    private final EngineConfigurationService engineConfigurationService;
    private final StatusService statusService;
    private final TransactionService transactionService;
    private static final Logger LOGGER = Logger.getLogger(ComServerAliveLoopImpl.class.getName());

    ComServerAliveLoopImpl(Clock clock, EngineConfigurationService engineConfigurationService, StatusService statusService, TransactionService transactionService) {
        this.clock = clock;
        this.engineConfigurationService = engineConfigurationService;
        this.statusService = statusService;
        executor = new ScheduledThreadPoolExecutor(1);
        this.transactionService = transactionService;
    }

    private void updateStatus() {
        engineConfigurationService.findComServerBySystemName().ifPresent(
                comServer -> {
                    ComServerStatus status = statusService.getStatus();
                    if (status.isBlocked()) {
                        engineConfigurationService.findOrCreateAliveStatus(comServer).update(clock.instant(),
                                engineConfigurationService.getComServerStatusAliveFrequency(),
                                status.getBlockTimestamp(), (int) status.getBlockTime().getSeconds());
                    } else {
                        engineConfigurationService.findOrCreateAliveStatus(comServer).update(clock.instant(),
                                engineConfigurationService.getComServerStatusAliveFrequency(),
                                null, null);
                    }
                }
        );
    }

    @Override
    public void run() {
        try (TransactionContext context = transactionService.getContext()) {
            updateStatus();
            executor.schedule(this, engineConfigurationService.getComServerStatusAliveFrequency(), TimeUnit.MINUTES);
            context.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
