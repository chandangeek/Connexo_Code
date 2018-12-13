/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.RemoteComServer;

import com.google.inject.Provider;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link OnlineComServerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-18 (13:22)
 */
@RunWith(MockitoJUnitRunner.class)
public class OnlineComServerImplTest extends PersistenceTest {

    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final String NO_VIOLATIONS_NAME = "Online-No-Violations";

    private static final String SERVER_NAME_PROPERTY = "serverName";
    private static final String EVENT_REGISTRATION_PORT_PROPERTY = "eventRegistrationPort";
    private static final String MONITOR_PORT_PROPERTY = "statusPort";

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    @Transactional
    public void testCreateWithoutComPortsWithoutViolations() throws SQLException {
        String name = NO_VIOLATIONS_NAME + 1;
        OnlineComServer comServer = this.createWithoutComPortsWithoutViolations(name);

        // Asserts
        assertThat(name).isEqualTo(comServer.getName());
        assertThat(comServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(comServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(comServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(comServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(comServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS + "}")
    public void testNameWithInvalidCharacters() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName("serverName");
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();
    }

    @Test
    @Transactional
    public void testNameWithValidCharacters() throws SQLException {
        createWithoutComPortsWithoutViolations("Legal.Name");
        createWithoutComPortsWithoutViolations("Legal-Name");
        createWithoutComPortsWithoutViolations("Legal0123456789Name");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_TOO_SMALL + "}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "testTooSmallChangesInterPollDelay";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        onlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_TOO_SMALL + "}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "testTooSmallSchedulingInterPollDelay";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        onlineComServer.create();
    }

    @Test
    @Transactional
    public void loadTest() throws SQLException {
        String name = NO_VIOLATIONS_NAME + 3;
        OnlineComServer createdComServer = this.createWithoutComPortsWithoutViolations(name);

        OnlineComServer loadedOnlineServer = (OnlineComServer) getEngineModelService().findComServer(createdComServer.getId()).get();

        // Asserts
        assertThat(name).isEqualTo(loadedOnlineServer.getName());
        assertThat(loadedOnlineServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(loadedOnlineServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(loadedOnlineServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(loadedOnlineServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(loadedOnlineServer.getSchedulingInterPollDelay());
        assertThat(ComServer.DEFAULT_QUERY_API_PORT_NUMBER).isEqualTo(loadedOnlineServer.getQueryApiPort());
    }

    @Test
    @Transactional
    public void testCreateWithValidEventRegistrationURI() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "Valid-event-registration-URL";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        // Asserts
        assertThat(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER).isEqualTo(onlineComServer.getEventRegistrationPort());
        assertThat(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, name, Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER))).isEqualTo(onlineComServer.getEventRegistrationUri());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskQueueSize")
    @Transactional
    public void testCreateWithTooSmallQueueSize() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.storeTaskQueueSize(OnlineComServer.MINIMUM_STORE_TASK_QUEUE_SIZE - 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskQueueSize")
    @Transactional
    public void testCreateWithTooBigQueueSize() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.storeTaskQueueSize(OnlineComServer.MAXIMUM_STORE_TASK_QUEUE_SIZE + 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "numberOfStoreTaskThreads")
    @Transactional
    public void testCreateWithTooSmallNumberOfThreads() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(OnlineComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS - 1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "numberOfStoreTaskThreads")
    @Transactional
    public void testCreateWithTooManyNumberOfThreads() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(OnlineComServer.MAXIMUM_NUMBER_OF_STORE_TASK_THREADS + 1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskThreadPriority")
    @Transactional
    public void testCreateWithTooLowThreadPriority() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.storeTaskThreadPriority(OnlineComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY - 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskThreadPriority")
    @Transactional
    public void testCreateWithTooHighThreadPriority() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.storeTaskThreadPriority(OnlineComServer.MAXIMUM_STORE_TASK_THREAD_PRIORITY + 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "name")
    @Transactional
    public void testCreateWithoutName() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName("serverName");
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "serverLogLevel")
    @Transactional
    public void testCreateWithoutServerLogLevel() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "No-Server-LogLevel";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(null);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "communicationLogLevel")
    @Transactional
    public void testCreateWithoutCommunicationLogLevel() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "No-Communication-LogLevel";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(null);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "changesInterPollDelay")
    @Transactional
    public void testCreateWithoutChangesInterPollDelay() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "No-Changes-InterpollDelay";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(null);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "schedulingInterPollDelay")
    @Transactional
    public void testCreateWithoutSchedulingInterPollDelay() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "No-Scheduling-InterpollDelay";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(null);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName(name);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER + "}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws SQLException {
        String name = "DuplicateExceptionExpected";
        this.createWithoutComPortsWithoutViolations(name);

        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        onlineComServer.name(name);
        onlineComServer.active(false);
        onlineComServer.serverLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        onlineComServer.serverName("serverName");
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        onlineComServer.create();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testUpdate() throws SQLException {
                OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "Update-Candidate";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        String changedName = "Name-Updated";
        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
        onlineComServer.setName(changedName);
        onlineComServer.setActive(false);
        onlineComServer.setServerLogLevel(changedServerLogLevel);
        onlineComServer.setCommunicationLogLevel(changedComLogLevel);
        onlineComServer.setChangesInterPollDelay(changedChangesInterPollDelay);
        onlineComServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
        onlineComServer.setServerName(changedName);
        int changedStatusPort = 9090;
        int changedEventRegistrationPort = 9999;
        onlineComServer.setStatusPort(changedStatusPort);
        onlineComServer.setEventRegistrationPort(changedEventRegistrationPort);

        // Business method
        onlineComServer.update();

        // Asserts
        assertThat(changedName).isEqualTo(onlineComServer.getName());
        assertThat(onlineComServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(onlineComServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(onlineComServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(onlineComServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(onlineComServer.getSchedulingInterPollDelay());
        assertThat(changedName).isEqualTo(onlineComServer.getServerName());
        assertThat(changedStatusPort).isEqualTo(onlineComServer.getStatusPort());
        assertThat(changedEventRegistrationPort).isEqualTo(onlineComServer.getEventRegistrationPort());
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class /*, message = "MDC.OnlineComServerXStillReferenced"*/)
    @Transactional
    public void testDeleteWhileStillUsedByRemoteComServer() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "testDeleteWhileStillUsedByRemoteComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.storeTaskQueueSize(5);
        onlineComServerBuilder.numberOfStoreTaskThreads(5);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServerBuilder = getEngineModelService().newRemoteComServerBuilder();
        String remoteName = "testDeleteWhileStillUsedByRemoteComServer-Remote";
        remoteComServerBuilder.name(remoteName);
        remoteComServerBuilder.active(true);
        remoteComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServerBuilder.onlineComServer(onlineComServer);
        remoteComServerBuilder.serverName(remoteName);
        remoteComServerBuilder.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final RemoteComServer remoteComServer = remoteComServerBuilder.create();

        // Business methods
        onlineComServer.delete();

        // We expect a cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class /* = "MDC.OnlineComServerXStillReferenced"*/)
    @Transactional
    public void testMakeObsoleteWhileStillUsedByRemoteComServer() throws SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "testMakeObsoleteWhileStillUsedByRemoteComServer";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.storeTaskQueueSize(5);
        onlineComServerBuilder.numberOfStoreTaskThreads(5);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServerBuilder = getEngineModelService().newRemoteComServerBuilder();
        String remoteName = "testMakeObsoleteWhileStillUsedByRemoteComServer-Remote";
        remoteComServerBuilder.name(remoteName);
        remoteComServerBuilder.active(true);
        remoteComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServerBuilder.onlineComServer(onlineComServer);
        remoteComServerBuilder.serverName(remoteName);
        remoteComServerBuilder.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServerBuilder.create();

        // Business methods
        onlineComServer.delete();

        // We expect a cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }


    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER_URI + "}", property = MONITOR_PORT_PROPERTY)
    @Transactional
    public void testUniqueStatusUriViolation() throws SQLException {
        createWithoutComPortsWithoutViolations("fist", "serverName", 8888, 8080);

        // Business method
        createWithoutComPortsWithoutViolations("2th", "serverName", 9999, 8080);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = SERVER_NAME_PROPERTY)
    @Transactional
    public void testInvalidUriViolationServerNameMissing() throws SQLException {
        createWithoutComPortsWithoutViolations("fist", null, 8888, 8080);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = EVENT_REGISTRATION_PORT_PROPERTY)
    @Transactional
    public void testInvalidStatusUriViolationEventRegistrationPortMissing() throws SQLException {
        createWithoutComPortsWithoutViolations("fist", 0, ComServer.DEFAULT_STATUS_PORT_NUMBER);
    }

    private OnlineComServer createWithoutComPortsWithoutViolations(String name) {
        return createWithoutComPortsWithoutViolations(name, ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER, ComServer.DEFAULT_STATUS_PORT_NUMBER);
    }

    private OnlineComServer createWithoutComPortsWithoutViolations(String name, int eventRegistrationPort, int statusPort) {
        return createWithoutComPortsWithoutViolations(name, name, eventRegistrationPort, statusPort);
    }

    private OnlineComServer createWithoutComPortsWithoutViolations(String name, String serverName, int eventRegistrationPort, int statusPort) {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.serverName(serverName);
        onlineComServerBuilder.queryApiPort(ComServer.DEFAULT_QUERY_API_PORT_NUMBER);
        onlineComServerBuilder.eventRegistrationPort(eventRegistrationPort);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);

        // Business method
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        return onlineComServer;
    }


}