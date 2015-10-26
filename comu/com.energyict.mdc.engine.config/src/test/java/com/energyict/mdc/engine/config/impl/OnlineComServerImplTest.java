package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.google.inject.Provider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link OnlineComServerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-18 (13:22)
 */
@RunWith(MockitoJUnitRunner.class)
public class OnlineComServerImplTest extends PersistenceTest {

    private static final String QUERY_API_POST_URI = "ws://comserver.energyict.com/queryAPI";
    private static final String INVALID_URL = "http://Anything but a valid URL";
    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/custom/events/registration";

    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final String NO_VIOLATIONS_NAME = "Online-No-Violations";

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    @Transactional
    public void testCreateWithoutComPortsWithoutViolations() throws BusinessException, SQLException {
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
    public void testThatDefaultURIsAreApplied() throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME + 2;
        OnlineComServer comServer = this.createWithoutComPortsWithoutViolations(name);

        // Asserts
        assertThat(comServer.getEventRegistrationUri()).isNotNull();
        assertThat(comServer.getQueryApiPostUri()).isNotNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS + "}")
    public void testNameWithInvalidCharacters() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();
    }

    @Test
    @Transactional
    public void testNameWithValidCharacters() throws BusinessException, SQLException {
        createWithoutComPortsWithoutViolations("Legal.Name");
        createWithoutComPortsWithoutViolations("Legal-Name");
        createWithoutComPortsWithoutViolations("Legal0123456789Name");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_TOO_SMALL + "}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        onlineComServer.name("testTooSmallChangesInterPollDelay");
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        onlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_TOO_SMALL + "}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        onlineComServer.name("testTooSmallSchedulingInterPollDelay");
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        onlineComServer.create();
    }

    @Test
    @Transactional
    public void loadTest() throws BusinessException, SQLException {
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
        assertThat(QUERY_API_POST_URI).isEqualTo(loadedOnlineServer.getQueryApiPostUri());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_INVALID_URL + "}", property = "queryAPIPostUri")
    @Transactional
    public void testCreateWithInvalidQueryAPIURL() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "WithComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(INVALID_URL);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an BusinessException
    }

    @Test
    @Transactional
    public void testCreateWithValidEventRegistrationURI() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "Valid-event-registration-URL";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.eventRegistrationUri(EVENT_REGISTRATION_URI);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);

        // Business method
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        // Asserts
        assertThat(EVENT_REGISTRATION_URI).isEqualTo(onlineComServer.getEventRegistrationUri());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_INVALID_URL + "}", property = "eventRegistrationUri")
    @Transactional
    public void testCreateWithInvalidEventRegistrationURI() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "Invalid-event-registration-URL";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.eventRegistrationUri(INVALID_URL);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an BusinessException because the event registration URL is not valid
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskQueueSize")
    @Transactional
    public void testCreateWithTooSmallQueueSize() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.storeTaskQueueSize(OnlineComServer.MINIMUM_STORE_TASK_QUEUE_SIZE - 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskQueueSize")
    @Transactional
    public void testCreateWithTooBigQueueSize() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.storeTaskQueueSize(OnlineComServer.MAXIMUM_STORE_TASK_QUEUE_SIZE + 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "numberOfStoreTaskThreads")
    @Transactional
    public void testCreateWithTooSmallNumberOfThreads() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.numberOfStoreTaskThreads(OnlineComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS - 1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "numberOfStoreTaskThreads")
    @Transactional
    public void testCreateWithTooManyNumberOfThreads() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.numberOfStoreTaskThreads(OnlineComServer.MAXIMUM_NUMBER_OF_STORE_TASK_THREADS + 1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskThreadPriority")
    @Transactional
    public void testCreateWithTooLowThreadPriority() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.storeTaskThreadPriority(OnlineComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY - 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "storeTaskThreadPriority")
    @Transactional
    public void testCreateWithTooHighThreadPriority() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "With-ComPort";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.storeTaskThreadPriority(OnlineComServer.MAXIMUM_STORE_TASK_THREAD_PRIORITY + 1);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting an TranslatableApplicationException
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "name")
    @Transactional
    public void testCreateWithoutName() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "serverLogLevel")
    @Transactional
    public void testCreateWithoutServerLogLevel() throws BusinessException, SQLException {
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

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because the server log level is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "communicationLogLevel")
    @Transactional
    public void testCreateWithoutCommunicationLogLevel() throws BusinessException, SQLException {
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

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because the communication log level is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "changesInterPollDelay")
    @Transactional
    public void testCreateWithoutChangesInterPollDelay() throws BusinessException, SQLException {
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

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because the changes interpoll delay is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "schedulingInterPollDelay")
    @Transactional
    public void testCreateWithoutSchedulingInterPollDelay() throws BusinessException, SQLException {
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

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because the scheduling interpoll delay is not set
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER + "}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
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

        // Business method
        onlineComServer.create();

        // Expecting a BusinessException to be thrown because a ComServer with the same name already exists
    }

    @Test
    @Transactional
    public void testUpdate() throws BusinessException, SQLException {
                OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        String name = "Update-Candidate";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServerBuilder.eventRegistrationUri(EVENT_REGISTRATION_URI);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);
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
        String changedQueryAPIPostUrl = QUERY_API_POST_URI + "?test=true";
        onlineComServer.setQueryAPIPostUri(changedQueryAPIPostUrl);

        // Business method
        onlineComServer.update();

        // Asserts
        assertThat(changedName).isEqualTo(onlineComServer.getName());
        assertThat(onlineComServer.isActive()).isFalse();
        assertThat(changedServerLogLevel).isEqualTo(onlineComServer.getServerLogLevel());
        assertThat(changedComLogLevel).isEqualTo(onlineComServer.getCommunicationLogLevel());
        assertThat(changedChangesInterPollDelay).isEqualTo(onlineComServer.getChangesInterPollDelay());
        assertThat(changedSchedulingInterPollDelay).isEqualTo(onlineComServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testUpdateNameAlsoUpdatesQueryAndEventURIs() throws BusinessException, SQLException {
                OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "willChangeSoon";
        onlineComServer.name(name);
//        onlineComServer.active(rue);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        final OnlineComServer comServer = onlineComServer.create();

        String queryApiPostUri = comServer.getQueryApiPostUri();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        String changedName = "changed";
        comServer.setName(changedName);

        // Business method
        comServer.update();

        // Asserts
        assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
        assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    public void testResetToDefaultQueryAndEventURIsViaShadowBooleanMethod() throws BusinessException, SQLException {
                OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "withCustomURIs";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        final OnlineComServer comServer = onlineComServer.create();

        String queryApiPostUri = comServer.getQueryApiPostUri();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        comServer.setUsesDefaultEventRegistrationUri(true);
        comServer.setUsesDefaultQueryAPIPostUri(true);

        // Business method
        comServer.update();

        // Asserts
        assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
        assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
        assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
        assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    public void testResetToDefaultQueryAndEventURIsViaNullEventURI() throws BusinessException, SQLException {
                OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "withCustomURIs";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        final OnlineComServer comServer = onlineComServer.create();

        String queryApiPostUri = comServer.getQueryApiPostUri();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        comServer.setEventRegistrationUri(null);
        comServer.setQueryAPIPostUri(null);

        // Business method
        comServer.update();

        // Asserts
        assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
        assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
        assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
        assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    public void testUpdateNameDoesNotUpdateQueryAndEventURIsIfTheyWereOverruledIntheFirstPlace() throws BusinessException, SQLException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();

        String name = "willBeModifiedSoon";
        onlineComServer.name(name);
        onlineComServer.active(true);
        onlineComServer.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        onlineComServer.numberOfStoreTaskThreads(1);
        onlineComServer.storeTaskThreadPriority(1);
        onlineComServer.storeTaskQueueSize(1);
        final OnlineComServer comServer = onlineComServer.create();

        String changedName = "modified";
        comServer.setName(changedName);

        // Business method
        comServer.update();

        // Asserts
        assertThat(QUERY_API_POST_URI).isEqualTo(comServer.getQueryApiPostUri());
        assertThat(EVENT_REGISTRATION_URI).isEqualTo(comServer.getEventRegistrationUri());
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class /*, message = "MDC.OnlineComServerXStillReferenced"*/)
    @Transactional
    public void testDeleteWhileStillUsedByRemoteComServer() throws BusinessException, SQLException {
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
        onlineComServerBuilder.queryApiPostUri(QUERY_API_POST_URI);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServerBuilder = getEngineModelService().newRemoteComServerBuilder();
        remoteComServerBuilder.name("testDeleteWhileStillUsedByRemoteComServer-Remote");
        remoteComServerBuilder.active(true);
        remoteComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServerBuilder.onlineComServer(onlineComServer);

        final RemoteComServer remoteComServer = remoteComServerBuilder.create();

        // Business methods
        onlineComServer.delete();

        // We expect a BusinessException, cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class /* = "MDC.OnlineComServerXStillReferenced"*/)
    @Transactional
    public void testMakeObsoleteWhileStillUsedByRemoteComServer() throws BusinessException, SQLException {
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
        onlineComServerBuilder.queryApiPostUri(QUERY_API_POST_URI);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServerBuilder = getEngineModelService().newRemoteComServerBuilder();
        remoteComServerBuilder.name("testMakeObsoleteWhileStillUsedByRemoteComServer-Remote");
        remoteComServerBuilder.active(true);
        remoteComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServerBuilder.onlineComServer(onlineComServer);

        remoteComServerBuilder.create();

        // Business methods
        onlineComServer.delete();

        // We expect a BusinessException, cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }

    private OnlineComServer createWithoutComPortsWithoutViolations(String name) throws SQLException, BusinessException {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();

        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        onlineComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServerBuilder.queryApiPostUri(QUERY_API_POST_URI);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.storeTaskQueueSize(1);

        // Business method
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        return onlineComServer;
    }


}