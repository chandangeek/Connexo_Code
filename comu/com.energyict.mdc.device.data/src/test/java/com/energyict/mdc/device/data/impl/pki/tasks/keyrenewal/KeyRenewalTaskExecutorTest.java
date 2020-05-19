package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapperDAO;
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
public class KeyRenewalTaskExecutorTest {

    @Mock
    public EventService eventService;

    @Mock
    public BpmService bpmService;

    @Mock
    public Clock clock;

    @Mock
    public SymmetricKeyWrapperDAO symmetricKeyWrapperDAO;

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
    public void noExpiredKeys() {
        int keyRenewalExpitationDays = 30;
        String id = "id";

        Instant aTime = Instant.now();
        Mockito.when(clock.instant()).thenReturn(aTime);

        ArrayList<SymmetricKeyWrapper> emptyList = new ArrayList<>();
        Instant keyExpirationTime = aTime.plus(Duration.ofDays(keyRenewalExpitationDays));
        Mockito.when(symmetricKeyWrapperDAO.findExpired(keyExpirationTime)).thenReturn(emptyList);
        KeyRenewalTaskExecutor keyRenewalTaskExecutor = new KeyRenewalTaskExecutor(eventService, bpmService, clock, symmetricKeyWrapperDAO, securityAccessorDAO, commandExecutorFactory, id, keyRenewalExpitationDays, logger);

        keyRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(symmetricKeyWrapperDAO, Mockito.times(1)).findExpired(keyExpirationTime);
        Mockito.verifyZeroInteractions(commandExecutorFactory);
    }

    @Test
    public void oneExpiredKeyNotLinked() {
        int keyRenewalExpitationDays = 30;
        String id = "id";

        Instant now = Instant.now();
        Mockito.when(clock.instant()).thenReturn(now);

        SymmetricKeyWrapper key1 = Mockito.mock(SymmetricKeyWrapper.class);
        ArrayList<SymmetricKeyWrapper> expiredKeys = new ArrayList<>();
        expiredKeys.add(key1);

        Instant keyExpirationTime = now.plus(Duration.ofDays(keyRenewalExpitationDays));
        Mockito.when(symmetricKeyWrapperDAO.findExpired(keyExpirationTime)).thenReturn(expiredKeys);

        Mockito.when(commandExecutorFactory.renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger))).thenReturn(commandExecutor);
        Mockito.when(securityAccessorDAO.findBy(key1)).thenReturn(Optional.ofNullable(null));

        KeyRenewalTaskExecutor keyRenewalTaskExecutor = new KeyRenewalTaskExecutor(eventService, bpmService, clock, symmetricKeyWrapperDAO, securityAccessorDAO, commandExecutorFactory, id, keyRenewalExpitationDays, logger);

        keyRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(symmetricKeyWrapperDAO, Mockito.times(1)).findExpired(keyExpirationTime);
        Mockito.verify(commandExecutorFactory, Mockito.times(1)).renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger));
        Mockito.verifyZeroInteractions(commandExecutor);
    }

    @Test
    public void oneExpiredKeyLinked() {
        int keyRenewalExpitationDays = 30;
        String id = "id";

        Instant now = Instant.now();
        Mockito.when(clock.instant()).thenReturn(now);

        SymmetricKeyWrapper key1 = Mockito.mock(SymmetricKeyWrapper.class);
        Optional<SecurityAccessor> sec1 = Optional.of(Mockito.mock(SecurityAccessor.class));
        ArrayList<SymmetricKeyWrapper> expiredKeys = new ArrayList<>();
        expiredKeys.add(key1);

        Instant keyExpirationTime = now.plus(Duration.ofDays(keyRenewalExpitationDays));
        Mockito.when(symmetricKeyWrapperDAO.findExpired(keyExpirationTime)).thenReturn(expiredKeys);

        Mockito.when(commandExecutorFactory.renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger))).thenReturn(commandExecutor);
        Mockito.when(securityAccessorDAO.findBy(key1)).thenReturn(sec1);

        KeyRenewalTaskExecutor keyRenewalTaskExecutor = new KeyRenewalTaskExecutor(eventService, bpmService, clock, symmetricKeyWrapperDAO, securityAccessorDAO, commandExecutorFactory, id, keyRenewalExpitationDays, logger);

        keyRenewalTaskExecutor.execute(taskOccurrence);

        Mockito.verify(symmetricKeyWrapperDAO, Mockito.times(1)).findExpired(keyExpirationTime);
        Mockito.verify(commandExecutorFactory, Mockito.times(1)).renewal(Mockito.any(CommandErrorHandler.class), Mockito.eq(bpmService), Mockito.eq(id), Mockito.eq(now), Mockito.eq(logger));
        Mockito.verify(securityAccessorDAO, Mockito.times(1)).findBy(key1);
        Mockito.verify(commandExecutor, Mockito.times(1)).execute(sec1.get());
        Mockito.verifyNoMoreInteractions(symmetricKeyWrapperDAO, commandExecutorFactory, securityAccessorDAO, commandExecutor);

    }


}
