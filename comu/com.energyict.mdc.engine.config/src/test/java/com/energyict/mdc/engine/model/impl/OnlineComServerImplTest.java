package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.google.inject.Provider;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link OnlineComServerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-18 (13:22)
 */
@RunWith(MockitoJUnitRunner.class)
public class OnlineComServerImplTest extends PersistenceTest {

    private static final String QUERY_API_POST_URI = "http://comserver.energyict.com/queryAPI";
    private static final String INVALID_URL = "Anything but a valid URL";
    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/custom/events/registration";

    private static final com.energyict.mdc.engine.model.ComServer.LogLevel SERVER_LOG_LEVEL = com.energyict.mdc.engine.model.ComServer.LogLevel.ERROR;
    private static final com.energyict.mdc.engine.model.ComServer.LogLevel COMMUNICATION_LOG_LEVEL = com.energyict.mdc.engine.model.ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);
    private static final String NO_VIOLATIONS_NAME = "Online-No-Violations";
    private static final String QUERY_API_PASSWORD = "API_password";
    private static final String QUERY_API_USER_NAME = "API_user";
    
    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    public void testGetTypeDoesNotReturnServerBasedClassName () {
        OnlineComServer onlineComServer = new OnlineComServerImpl(dataModel, getEngineModelService(), outboundComPortProvider);

        // Business method
        String type = onlineComServer.getType();

        // Asserts
        assertThat(type).doesNotContain(".Server");
    }

    @Test
    public void testCreateWithoutComPortsWithoutViolations () throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME+1;
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
    public void testThatDefaultURIsAreApplied () throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME+2;
        OnlineComServer comServer = this.createWithoutComPortsWithoutViolations(name);

        // Asserts
        assertThat(comServer.getEventRegistrationUri()).isNotNull();
        assertThat(comServer.getQueryApiPostUri()).isNotNull();
    }

    private OnlineComServer createWithoutComPortsWithoutViolations (String name) throws SQLException, BusinessException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            onlineComServer.setName(name);
            onlineComServer.setActive(true);
            onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
            onlineComServer.setNumberOfStoreTaskThreads(1);
            onlineComServer.setStoreTaskThreadPriority(1);
            onlineComServer.setStoreTaskQueueSize(1);

            // Business method
            onlineComServer.save();
            context.commit();
            return onlineComServer;
        }
    }

    @Test
    public void testNameWithInvalidCharacters () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        onlineComServer.setName("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);

        try {
            // Business method
            onlineComServer.save();
            failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
        }
        catch (TranslatableApplicationException e) {
            // Asserts
            assertThat("nameXcontainsInvalidChars").isEqualTo(e.getMessageId());
        }
    }

    @Test
    public void testTooSmallChangesInterPollDelay () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        onlineComServer.setName("testTooSmallChangesInterPollDelay");
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);

        try {
            // Business method
            onlineComServer.save();
            failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
        }
        catch (TranslatableApplicationException e) {
            // Asserts
            assertThat("XshouldBeAtLeast").isEqualTo(e.getMessageId());
        }
    }

    @Test
    public void testTooSmallSchedulingInterPollDelay () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        onlineComServer.setName("testTooSmallSchedulingInterPollDelay");
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);

        try {
            // Business method
            onlineComServer.save();
            failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
        }
        catch (TranslatableApplicationException e) {
            // Asserts
            assertThat("XshouldBeAtLeast").isEqualTo(e.getMessageId());
        }
    }

    @Test
    public void loadTest() throws BusinessException, SQLException {
        String name = NO_VIOLATIONS_NAME+3;
        OnlineComServer createdComServer = this.createWithoutComPortsWithoutViolations(name);

        OnlineComServer loadedOnlineServer = (OnlineComServer) getEngineModelService().findComServer((int) createdComServer.getId());

        // Asserts
        assertThat(name).isEqualTo(loadedOnlineServer.getName());
        assertThat(loadedOnlineServer.isActive()).isTrue();
        assertThat(SERVER_LOG_LEVEL).isEqualTo(loadedOnlineServer.getServerLogLevel());
        assertThat(COMMUNICATION_LOG_LEVEL).isEqualTo(loadedOnlineServer.getCommunicationLogLevel());
        assertThat(CHANGES_INTER_POLL_DELAY).isEqualTo(loadedOnlineServer.getChangesInterPollDelay());
        assertThat(SCHEDULING_INTER_POLL_DELAY).isEqualTo(loadedOnlineServer.getSchedulingInterPollDelay());
        assertThat(QUERY_API_POST_URI).isEqualTo(loadedOnlineServer.getQueryApiPostUri());
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithInvalidQueryAPIURL () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(INVALID_URL);

        // Business method
        onlineComServer.save();

        // Expecting an BusinessException
    }

    @Test
    public void testCreateWithValidEventRegistrationURI () throws BusinessException, SQLException {
        try (TransactionContext context=getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            String name = "Valid-event-registration-URL";
            onlineComServer.setName(name);
            onlineComServer.setActive(true);
            onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            onlineComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            onlineComServer.setNumberOfStoreTaskThreads(1);
            onlineComServer.setStoreTaskThreadPriority(1);
            onlineComServer.setStoreTaskQueueSize(1);

            // Business method
            onlineComServer.save();

            // Asserts
            assertThat(EVENT_REGISTRATION_URI).isEqualTo(onlineComServer.getEventRegistrationUri());
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithInvalidEventRegistrationURI () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "Invalid-event-registration-URL";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setEventRegistrationUri(INVALID_URL);

        // Business method
        onlineComServer.save();

        // Expecting an BusinessException because the event registration URL is not valid
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooSmallQueueSize () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setStoreTaskQueueSize(OnlineComServer.MINIMUM_STORE_TASK_QUEUE_SIZE - 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooBigQueueSize () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setStoreTaskQueueSize(OnlineComServer.MAXIMUM_STORE_TASK_QUEUE_SIZE + 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooSmallNumberOfThreads () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setNumberOfStoreTaskThreads(OnlineComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS - 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooManyNumberOfThreads () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setNumberOfStoreTaskThreads(OnlineComServer.MAXIMUM_NUMBER_OF_STORE_TASK_THREADS + 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooLowThreadPriority () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setStoreTaskThreadPriority(OnlineComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY - 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithTooHighThreadPriority () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "With-ComPort";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.setStoreTaskThreadPriority(OnlineComServer.MAXIMUM_STORE_TASK_THREAD_PRIORITY + 1);

        // Business method
        onlineComServer.save();

        // Expecting an TranslatableApplicationException
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutName () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutServerLogLevel () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "No-Server-LogLevel";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(null);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because the server log level is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutCommunicationLogLevel () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "No-Communication-LogLevel";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(null);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because the communication log level is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutChangesInterPollDelay () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "No-Changes-InterpollDelay";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(null);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because the changes interpoll delay is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutSchedulingInterPollDelay () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        String name = "No-Scheduling-InterpollDelay";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(null);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because the scheduling interpoll delay is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithExistingName () throws BusinessException, SQLException {
        String name = "DuplicateExceptionExpected";
        this.createWithoutComPortsWithoutViolations(name);

        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance(); 
        onlineComServer.setName(name);
        onlineComServer.setActive(false);
        onlineComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        // Business method
        onlineComServer.save();

        // Expecting a BusinessException to be thrown because a ComServer with the same name already exists
    }

    @Test
    public void testUpdate () throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer comServer = getEngineModelService().newOnlineComServerInstance();
            String name = "Update-Candidate";
            comServer.setName(name);
            comServer.setActive(true);
            comServer.setServerLogLevel(SERVER_LOG_LEVEL);
            comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            comServer.setQueryAPIPostUri(QUERY_API_POST_URI);
            comServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            comServer.setNumberOfStoreTaskThreads(1);
            comServer.setStoreTaskThreadPriority(1);
            comServer.setStoreTaskQueueSize(1);
            comServer.save();

            String changedName = "Name-Updated";
            ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
            ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
            TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
            TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
            comServer.setName(changedName);
            comServer.setActive(false);
            comServer.setServerLogLevel(changedServerLogLevel);
            comServer.setCommunicationLogLevel(changedComLogLevel);
            comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
            comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
            String changedQueryAPIPostUrl = QUERY_API_POST_URI + "?test=true";
            comServer.setQueryAPIPostUri(changedQueryAPIPostUrl);

            // Business method
            comServer.save();

            // Asserts
            assertThat(changedName).isEqualTo(comServer.getName());
            assertThat(comServer.isActive()).isFalse();
            assertThat(changedServerLogLevel).isEqualTo(comServer.getServerLogLevel());
            assertThat(changedComLogLevel).isEqualTo(comServer.getCommunicationLogLevel());
            assertThat(changedChangesInterPollDelay).isEqualTo(comServer.getChangesInterPollDelay());
            assertThat(changedSchedulingInterPollDelay).isEqualTo(comServer.getSchedulingInterPollDelay());
        }
    }

    @Test
    public void testUpdateNameAlsoUpdatesQueryAndEventURIs () throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer comServer = getEngineModelService().newOnlineComServerInstance();
            String name = "willChangeSoon";
            comServer.setName(name);
            comServer.setActive(true);
            comServer.setServerLogLevel(SERVER_LOG_LEVEL);
            comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            comServer.setNumberOfStoreTaskThreads(1);
            comServer.setStoreTaskThreadPriority(1);
            comServer.setStoreTaskQueueSize(1);
            comServer.save();

            String queryApiPostUri = comServer.getQueryApiPostUri();
            String eventRegistrationUri = comServer.getEventRegistrationUri();

            String changedName = "changed";
            comServer.setName(changedName);

            // Business method
            comServer.save();

            // Asserts
            assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
            assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
        }
    }

    @Test
    public void testResetToDefaultQueryAndEventURIsViaShadowBooleanMethod () throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer comServer = getEngineModelService().newOnlineComServerInstance();
            String name = "withCustomURIs";
            comServer.setName(name);
            comServer.setActive(true);
            comServer.setServerLogLevel(SERVER_LOG_LEVEL);
            comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            comServer.setQueryAPIPostUri(QUERY_API_POST_URI);
            comServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            comServer.setNumberOfStoreTaskThreads(1);
            comServer.setStoreTaskThreadPriority(1);
            comServer.setStoreTaskQueueSize(1);
            comServer.save();

            String queryApiPostUri = comServer.getQueryApiPostUri();
            String eventRegistrationUri = comServer.getEventRegistrationUri();

            comServer.setUsesDefaultEventRegistrationUri(true);
            comServer.setUsesDefaultQueryAPIPostUri(true);

            // Business method
            comServer.save();

            // Asserts
            assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
            assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
            assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
            assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
        }
    }

    @Test
    public void testResetToDefaultQueryAndEventURIsViaNullEventURI () throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer comServer = getEngineModelService().newOnlineComServerInstance();
            String name = "withCustomURIs";
            comServer.setName(name);
            comServer.setActive(true);
            comServer.setServerLogLevel(SERVER_LOG_LEVEL);
            comServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            comServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            comServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            comServer.setQueryAPIPostUri(QUERY_API_POST_URI);
            comServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            comServer.setNumberOfStoreTaskThreads(1);
            comServer.setStoreTaskThreadPriority(1);
            comServer.setStoreTaskQueueSize(1);
            comServer.save();

            String queryApiPostUri = comServer.getQueryApiPostUri();
            String eventRegistrationUri = comServer.getEventRegistrationUri();

            comServer.setEventRegistrationUri(null);
            comServer.setQueryAPIPostUri(null);

            // Business method
            comServer.save();

            // Asserts
            assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
            assertThat(queryApiPostUri).isNotEqualTo(comServer.getQueryApiPostUri());
            assertThat(comServer.usesDefaultEventRegistrationUri()).isTrue();
            assertThat(eventRegistrationUri).isNotEqualTo(comServer.getEventRegistrationUri());
        }
    }

    @Test
    public void testUpdateNameDoesNotUpdateQueryAndEventURIsIfTheyWereOverruledIntheFirstPlace () throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
            String name = "willBeModifiedSoon";
            onlineComServer.setName(name);
            onlineComServer.setActive(true);
            onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
            onlineComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            onlineComServer.setNumberOfStoreTaskThreads(1);
            onlineComServer.setStoreTaskThreadPriority(1);
            onlineComServer.setStoreTaskQueueSize(1);
            onlineComServer.save();

            String changedName = "modified";
            onlineComServer.setName(changedName);

            // Business method
            onlineComServer.save();

            // Asserts
            assertThat(QUERY_API_POST_URI).isEqualTo(onlineComServer.getQueryApiPostUri());
            assertThat(EVENT_REGISTRATION_URI).isEqualTo(onlineComServer.getEventRegistrationUri());
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testDeleteWhileStillUsedByRemoteComServer() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        String name = "testDeleteWhileStillUsedByRemoteComServer";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.save();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("testDeleteWhileStillUsedByRemoteComServer-Remote");
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
        remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

        remoteComServer.save();

        // Business methods
        onlineComServer.delete();

        // We expect a BusinessException, cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testMakeObsoleteWhileStillUsedByRemoteComServer () throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        String name = "testMakeObsoleteWhileStillUsedByRemoteComServer";
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setQueryAPIPostUri(QUERY_API_POST_URI);
        onlineComServer.save();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("testMakeObsoleteWhileStillUsedByRemoteComServer-Remote");
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
        remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

        remoteComServer.save();

        // Business methods
        onlineComServer.delete();

        // We expect a BusinessException, cause an OnlineComServer cannot be deleted when it is still referenced from a RemoteComServer
    }

}