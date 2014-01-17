package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.google.inject.Provider;
import java.sql.SQLException;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
* Tests the {@link RemoteComServerImpl} component.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-18 (16:52)
*/
public class RemoteComServerImplTest extends PersistenceTest {

    private static int onlineNameNumber = 1;

    private static final String NO_VIOLATIONS_NAME = "Remote-No-Violations";
    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);
    private static final String QUERY_API_USER_NAME = "johndoe";
    private static final String QUERY_API_PASSWORD = "doe";
    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/custom/events/registration";
    private static final String INVALID_URI = "Anything but a valid URI";

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    public void testGetTypeDoesNotReturnServerBasedClassName() {
        RemoteComServer onlineComServer = new RemoteComServerImpl(dataModel, getEngineModelService(), outboundComPortProvider, null, null, null);

        // Business method
        String type = onlineComServer.getType();

        // Asserts
        assertThat(type).doesNotContain(".Server");
    }

    @Test
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = NO_VIOLATIONS_NAME;
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            // Business method
            remoteComServer.save();
            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);

            // Asserts
            assertEquals(name, comServer.getName());
            assertTrue("Was expecting the new com server to be active", comServer.isActive());
            assertEquals(SERVER_LOG_LEVEL, comServer.getServerLogLevel());
            assertEquals(COMMUNICATION_LOG_LEVEL, comServer.getCommunicationLogLevel());
            assertEquals(CHANGES_INTER_POLL_DELAY, comServer.getChangesInterPollDelay());
            assertEquals(SCHEDULING_INTER_POLL_DELAY, comServer.getSchedulingInterPollDelay());
            assertEquals(QUERY_API_USER_NAME, comServer.getQueryAPIUsername());
            assertEquals(QUERY_API_PASSWORD, comServer.getQueryAPIPassword());
            assertEquals(onlineComServer.getId(), comServer.getOnlineComServer().getId());
        }
    }

    @Test
    public void testThatDefaultURIsAreApplied() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = NO_VIOLATIONS_NAME;
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            // Business method
            remoteComServer.save();
            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);

            // Asserts
            assertTrue(comServer.usesDefaultEventRegistrationUri());
            assertNotNull(comServer.getEventRegistrationUri());
        }
    }

    @Test
    public void testNameWithInvalidCharacters() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            remoteComServer.setName("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            try {
                // Business method
                remoteComServer.save();
                failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
            } catch (TranslatableApplicationException e) {
                // Asserts
                assertEquals("nameXcontainsInvalidChars", e.getMessageId());
            }
        }
    }

    @Test
    public void testTooSmallChangesInterPollDelay() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            remoteComServer.setName("testTooSmallChangesInterPollDelay");
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            try {
                // Business method
                remoteComServer.save();
                failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
            } catch (TranslatableApplicationException e) {
                // Asserts
                assertEquals("XshouldBeAtLeast", e.getMessageId());
            }
        }
    }

    @Test
    public void testTooSmallSchedulingInterPollDelay() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            remoteComServer.setName("testTooSmallSchedulingInterPollDelay");
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            try {
                // Business method
                remoteComServer.save();
                failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
            } catch (TranslatableApplicationException e) {
                // Asserts
                assertEquals("XshouldBeAtLeast", e.getMessageId());
            }
        }
    }

    @Test
    public void testCreateWithValidEventRegistrationURI() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = NO_VIOLATIONS_NAME;
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);
            remoteComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);

            // Business method
            remoteComServer.save();
            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);

            // Asserts
            assertEquals(EVENT_REGISTRATION_URI, comServer.getEventRegistrationUri());
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithInvalidEventRegistrationURI() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = NO_VIOLATIONS_NAME;
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);
            remoteComServer.setEventRegistrationUri(INVALID_URI);

            // Business method
            remoteComServer.save();

            // Was expecting a BusinessException because the event registration URL is not valid
        }
    }

    @Test
    public void loadTest() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = NO_VIOLATIONS_NAME;
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            // Business method
            remoteComServer.save();
            RemoteComServer loadedRemoteComServer = (RemoteComServer) getEngineModelService().findComServer(remoteComServer.getId());

            // asserts
            assertNotNull(loadedRemoteComServer);
            assertEquals(name, loadedRemoteComServer.getName());
            assertTrue("Was expecting the new com server to be active", loadedRemoteComServer.isActive());
            assertEquals(SERVER_LOG_LEVEL, loadedRemoteComServer.getServerLogLevel());
            assertEquals(COMMUNICATION_LOG_LEVEL, loadedRemoteComServer.getCommunicationLogLevel());
            assertEquals(CHANGES_INTER_POLL_DELAY, loadedRemoteComServer.getChangesInterPollDelay());
            assertEquals(SCHEDULING_INTER_POLL_DELAY, loadedRemoteComServer.getSchedulingInterPollDelay());
            assertEquals(QUERY_API_USER_NAME, loadedRemoteComServer.getQueryAPIUsername());
            assertEquals(QUERY_API_PASSWORD, loadedRemoteComServer.getQueryAPIPassword());
            assertNotNull("The OnlineComServer should not be null", loadedRemoteComServer.getOnlineComServer());
            assertEquals(onlineComServer.getId(), loadedRemoteComServer.getOnlineComServer().getId());
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutOnlineComServer() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "With-ComPort";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            remoteComServer.setQueryAPIPassword(QUERY_API_PASSWORD);

            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutName() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            // Business method
            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutServerLogLevel() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "No-Server-LogLevel";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(null);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutCommunicationLogLevel() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "No-Communication-LogLevel";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(null);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutChangesInterPollDelay() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "No-Changes-InterpollDelay";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(null);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithoutSchedulingInterPollDelay() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "No-Scheduling-InterpollDelay";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(null);

            remoteComServer.save();
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            remoteComServer.setName(NO_VIOLATIONS_NAME);
            remoteComServer.setActive(false);
            remoteComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
            remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            remoteComServer.save();
            RemoteComServer duplicateComServer = getEngineModelService().newRemoteComServerInstance();
            duplicateComServer.setName(NO_VIOLATIONS_NAME);
            duplicateComServer.setActive(false);
            duplicateComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
            duplicateComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
            duplicateComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            duplicateComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            duplicateComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

            duplicateComServer.save();
        }
    }

    @Test
    public void testUpdate() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "Update-Candidate";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            remoteComServer.save();

            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);

            // Business method
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
            comServer.setQueryAPIUsername(QUERY_API_USER_NAME);
            comServer.setQueryAPIPassword(QUERY_API_PASSWORD);
            comServer.save();

            // Asserts
            assertEquals(changedName, comServer.getName());
            assertFalse("Was NOT expecting the com server to be active after update", comServer.isActive());
            assertEquals(changedServerLogLevel, comServer.getServerLogLevel());
            assertEquals(changedComLogLevel, comServer.getCommunicationLogLevel());
            assertEquals(changedChangesInterPollDelay, comServer.getChangesInterPollDelay());
            assertEquals(changedSchedulingInterPollDelay, comServer.getSchedulingInterPollDelay());
            assertEquals(QUERY_API_USER_NAME, comServer.getQueryAPIUsername());
            assertEquals(QUERY_API_PASSWORD, comServer.getQueryAPIPassword());
        }
    }

    @Test
    public void testResetEventRegistrationAPIViaBooleanMethod() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "withCustomURI";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            remoteComServer.save();

            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);
            String eventRegistrationUri = comServer.getEventRegistrationUri();

            // Business method
            comServer.setUsesDefaultEventRegistrationUri(true);
            comServer.save();

            // Asserts
            assertTrue(comServer.usesDefaultEventRegistrationUri());
            assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
        }
    }

    @Test
    public void testResetEventRegistrationAPIViaNullURI() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "withCustomURI";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
            remoteComServer.save();

            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);
            String eventRegistrationUri = comServer.getEventRegistrationUri();

            // Business method
            comServer.setEventRegistrationUri(null);
            comServer.save();

            // Asserts
            assertTrue(comServer.usesDefaultEventRegistrationUri());
            assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
        }
    }

    @Test(expected = TranslatableApplicationException.class)
    public void testUpdateWithoutOnlineComServer() throws BusinessException, SQLException {
        try (TransactionContext context = getTransactionService().getContext()) {
            OnlineComServer onlineComServer = this.createOnlineComServer();

            RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
            String name = "Update-Candidate2";
            remoteComServer.setName(name);
            remoteComServer.setActive(true);
            remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
            remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
            remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
            remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
            remoteComServer.setOnlineComServer(onlineComServer);
            remoteComServer.save();

            RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name);

            // Business method
            String changedName = "Name-Updated2";
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
            comServer.setOnlineComServer(null);
            comServer.save();
        }
    }

    private OnlineComServer createOnlineComServer() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        String name = "Online-" + onlineNameNumber++;
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        onlineComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        onlineComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        onlineComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setNumberOfStoreTaskThreads(1);
        onlineComServer.save();
        return onlineComServer;
    }

}