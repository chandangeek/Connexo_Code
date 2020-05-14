/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.pki.CertificateDAO;
import com.elster.jupiter.pki.CertificateWrapper;
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

import static com.energyict.mdc.device.data.impl.pki.tasks.certrenewal.CertificateRenewalHandlerFactory.CERTIFICATE_RENEWAL_PROCESS_NAME;

public class CertificateRenewalTaskExecutor implements TaskExecutor {

    private static final Integer CERTIFICATE_RENEWAL_EXPIRATION_DAYS = Integer.MAX_VALUE;

    private final CertificateDAO certificateDAO;
    private final SecurityAccessorDAO securityAccessorDAO;
    private final EventService eventService;
    private final BpmService bpmService;
    private final Clock clock;
    private final String certRenewalBpmProcessDefinitionId;
    private final Integer certRenewalExpitationDays;
    private final Logger logger;
    private final CommandExecutorFactory commandExecutorFactory;


    CertificateRenewalTaskExecutor(CertificateDAO certificateDAO, SecurityAccessorDAO securityAccessorDAO, CommandExecutorFactory commandExecutorFactory,
                                   Clock clock, EventService eventService,
                                   BpmService bpmService,
                                   String certRenewalBpmProcessDefinitionId,
                                   Integer certRenewalExpitationDays,
                                   Logger logger
    ) {
        this.certificateDAO = certificateDAO;
        this.securityAccessorDAO = securityAccessorDAO;
        this.commandExecutorFactory = commandExecutorFactory;
        this.clock = clock;
        this.eventService = eventService;
        this.bpmService = bpmService;
        this.certRenewalBpmProcessDefinitionId = Optional.ofNullable(certRenewalBpmProcessDefinitionId).orElse(CERTIFICATE_RENEWAL_PROCESS_NAME);
        this.certRenewalExpitationDays = Optional.ofNullable(certRenewalExpitationDays).orElse(CERTIFICATE_RENEWAL_EXPIRATION_DAYS);
        this.logger = logger;
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        // Here should we find only a certain type of certificates (client e.g)?
        Instant now = clock.instant();
        Instant expiration = now.plus(Duration.ofDays(certRenewalExpitationDays));
        List<CertificateWrapper> certificateWrappers = certificateDAO.findExpired(expiration);
        logger.log(Level.INFO, "Number of expired certificates found:" + certificateWrappers.size());

        if (certificateWrappers.isEmpty()) {
            return;
        }

        CommandExecutor commandExecutor = this.commandExecutorFactory.renewal(new CommandErrorHandler(this, occurrence, eventService, logger), bpmService, certRenewalBpmProcessDefinitionId, now, logger);
        certificateWrappers.parallelStream().map(f -> securityAccessorDAO.findBy(f)).filter(f -> f.isPresent()).map(f -> f.get()).forEach(f -> commandExecutor.execute(f));
    }


}
