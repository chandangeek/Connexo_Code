/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapperDAO;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandErrorHandler;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandExecutor;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandExecutorFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyRenewalTaskExecutor implements TaskExecutor {

    private static final String KEY_RENEWAL_PROCESS_NAME = "Key renewal";
    private static final Integer KEY_RENEWAL_EXPIRATION_DAYS = Integer.MAX_VALUE;

    private final EventService eventService;
    private final BpmService bpmService;
    private final Clock clock;
    private final SymmetricKeyWrapperDAO symmetricKeyWrapperDAO;
    private final SecurityAccessorDAO securityAccessorDAO;

    private final CommandExecutorFactory commandExecutorFactory;
    private final String keyRenewalBpmProcessDefinitionId;
    private final Integer keyRenewalExpitationDays;
    private final Logger logger;


    KeyRenewalTaskExecutor(EventService eventService, BpmService bpmService,
                           Clock clock,
                           SymmetricKeyWrapperDAO symmetricKeyWrapperDAO,
                           SecurityAccessorDAO securityAccessorDAO,
                           CommandExecutorFactory commandExecutorFactory,
                           String keyRenewalBpmProcessDefinitionId,
                           Integer keyRenewalExpitationDays,
                           Logger logger

    ) {
        this.eventService = eventService;
        this.bpmService = bpmService;
        this.clock = clock;
        this.symmetricKeyWrapperDAO = symmetricKeyWrapperDAO;
        this.securityAccessorDAO = securityAccessorDAO;
        this.commandExecutorFactory = commandExecutorFactory;
        this.keyRenewalBpmProcessDefinitionId = Optional.ofNullable(keyRenewalBpmProcessDefinitionId).orElse(KEY_RENEWAL_PROCESS_NAME);
        this.keyRenewalExpitationDays = Optional.ofNullable(keyRenewalExpitationDays).orElse(KEY_RENEWAL_EXPIRATION_DAYS);
        this.logger = logger;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());

        // why only symmetric keys? Don't know while I have no requirements, this is only a refactoring and this is how it was
        Instant now = clock.instant();
        List<SymmetricKeyWrapper> expiredKeys = symmetricKeyWrapperDAO.findExpired(now.plus(Duration.ofDays(keyRenewalExpitationDays)));
        logger.log(Level.INFO, "Number of expired keys found:" + expiredKeys.size());

        if (expiredKeys.isEmpty()) {
            return;
        }

        CommandExecutor commandExecutor = this.commandExecutorFactory.renewal(new CommandErrorHandler(this, occurrence, eventService, logger), bpmService, keyRenewalBpmProcessDefinitionId, now, logger);
        // in future perhaps this needs more configuration like using a configurable number of threads
        expiredKeys.stream().map(f -> securityAccessorDAO.findBy(f)).filter(f -> f.isPresent()).map(f -> f.get()).forEach(f -> commandExecutor.execute(f));

    }

}
