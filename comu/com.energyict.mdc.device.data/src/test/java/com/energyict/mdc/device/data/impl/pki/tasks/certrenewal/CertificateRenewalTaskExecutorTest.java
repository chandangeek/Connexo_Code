package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.pki.CertificateDAO;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.SecurityAccessorDAO;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandErrorHandler;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandExecutor;
import com.energyict.mdc.device.data.impl.pki.tasks.command.CommandExecutorFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CertificateRenewalTaskExecutorTest {

    @Mock
    public EventService eventService;

    @Mock
    public BpmService bpmService;

    @Mock
    public Clock clock;

    @Mock
    public CertificateDAO certificateDAO;

    @Mock
    public SecurityAccessorDAO securityAccessorDAO;

    @Mock
    public CommandExecutorFactory commandExecutorFactory;

    @Mock
    public CommandExecutor commandExecutor;

    @Mock
    public TaskOccurrence taskOccurrence;

    @Mock
    public TaskLogHandler taskLogHandler;

    @Mock
    public Logger logger;

    @Before
    public void setUp() {
        Mockito.when(taskOccurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        Mockito.when(taskLogHandler.asHandler()).thenReturn(new ConsoleHandler());
    }

    @Test
    public void noExpiredCertificates() {
        int certExpirationDays = 30;
        String id = "id";

        Instant aTime = Instant.now();
        Mockito.when(clock.instant()).thenReturn(aTime);

        ArrayList<CertificateWrapper> emptyList = new ArrayList<>();
        Instant certExpirationTime = aTime.plus(Duration.ofDays(certExpirationDays));
        Mockito.when(certificateDAO.findExpired(certExpirationTime)).thenReturn(emptyList);
        CertificateRenewalTaskExecutor certificateRenewalTaskExecutor = new CertificateRenewalTaskExecutor(certificateDAO, securityAccessorDAO, commandExecutorFactory, clock, eventService, bpmService, id, certExpirationDays, logger);

        certificateRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(certificateDAO, Mockito.times(1)).findExpired(certExpirationTime);
        Mockito.verifyZeroInteractions(commandExecutorFactory);
    }

    @Test
    public void oneExpiredCertificateNotLinked() {
        int certExpirationDays = 30;
        String id = "id";

        Instant now = Instant.now();
        Mockito.when(clock.instant()).thenReturn(now);

        CertificateWrapper cert1 = Mockito.mock(CertificateWrapper.class);
        ArrayList<CertificateWrapper> expiredCertificates = new ArrayList<>();
        expiredCertificates.add(cert1);

        Instant certExpirationTime = now.plus(Duration.ofDays(certExpirationDays));
        Mockito.when(certificateDAO.findExpired(certExpirationTime)).thenReturn(expiredCertificates);

        Mockito.when(commandExecutorFactory.renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger))).thenReturn(commandExecutor);
        Mockito.when(securityAccessorDAO.findBy(cert1)).thenReturn(Optional.ofNullable(null));

        CertificateRenewalTaskExecutor keyRenewalTaskExecutor = new CertificateRenewalTaskExecutor(certificateDAO, securityAccessorDAO, commandExecutorFactory, clock, eventService, bpmService, id, certExpirationDays, logger);

        keyRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(certificateDAO, Mockito.times(1)).findExpired(certExpirationTime);
        Mockito.verify(commandExecutorFactory, Mockito.times(1)).renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger));
        Mockito.verifyZeroInteractions(commandExecutor);
    }

    @Test
    public void oneExpiredCertificateLinked() {
        int certificateExpirationDays = 30;
        String id = "id";

        Instant now = Instant.now();
        Mockito.when(clock.instant()).thenReturn(now);

        CertificateWrapper cert1 = Mockito.mock(CertificateWrapper.class);
        Optional<SecurityAccessor> sec1 = Optional.of(Mockito.mock(SecurityAccessor.class));
        ArrayList<CertificateWrapper> expiredCertificates = new ArrayList<>();
        expiredCertificates.add(cert1);

        Instant certExpirationTime = now.plus(Duration.ofDays(certificateExpirationDays));
        Mockito.when(certificateDAO.findExpired(certExpirationTime)).thenReturn(expiredCertificates);

        Mockito.when(commandExecutorFactory.renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger))).thenReturn(commandExecutor);
        Mockito.when(securityAccessorDAO.findBy(cert1)).thenReturn(sec1);

        CertificateRenewalTaskExecutor keyRenewalTaskExecutor = new CertificateRenewalTaskExecutor(certificateDAO, securityAccessorDAO, commandExecutorFactory, clock, eventService, bpmService, id, certificateExpirationDays, logger);

        keyRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(certificateDAO, Mockito.times(1)).findExpired(certExpirationTime);
        Mockito.verify(commandExecutorFactory, Mockito.times(1)).renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger));
        Mockito.verify(securityAccessorDAO, Mockito.times(1)).findBy(cert1);
        Mockito.verify(commandExecutor, Mockito.times(1)).execute(sec1.get());
        Mockito.verifyNoMoreInteractions(certificateDAO, commandExecutorFactory, securityAccessorDAO, commandExecutor);

    }


}
