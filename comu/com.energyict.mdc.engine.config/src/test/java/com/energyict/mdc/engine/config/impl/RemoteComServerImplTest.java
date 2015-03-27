package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.google.inject.Provider;
import org.junit.Test;
import org.mockito.Mock;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final String EVENT_REGISTRATION_URI = "http://comserver.energyict.com/custom/events/registration";
    private static final String INVALID_URI = "Anything but a valid URI";

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
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

        // Business method
        remoteComServer.save();
        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

        // Asserts
        assertEquals(name, comServer.getName());
        assertTrue("Was expecting the new com server to be active", comServer.isActive());
        assertEquals(SERVER_LOG_LEVEL, comServer.getServerLogLevel());
        assertEquals(COMMUNICATION_LOG_LEVEL, comServer.getCommunicationLogLevel());
        assertEquals(CHANGES_INTER_POLL_DELAY, comServer.getChangesInterPollDelay());
        assertEquals(SCHEDULING_INTER_POLL_DELAY, comServer.getSchedulingInterPollDelay());
        assertEquals(onlineComServer.getId(), comServer.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    public void testThatDefaultURIsAreApplied() throws BusinessException, SQLException {
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

        // Business method
        remoteComServer.save();
        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

        // Asserts
        assertTrue(comServer.usesDefaultEventRegistrationUri());
        assertNotNull(comServer.getEventRegistrationUri());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS +"}", property = "name")
    @Transactional
    public void testNameWithInvalidCharacters() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(onlineComServer);

        remoteComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("testTooSmallChangesInterPollDelay");
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(onlineComServer);

        remoteComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("testTooSmallSchedulingInterPollDelay");
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.setOnlineComServer(onlineComServer);

        remoteComServer.save();
    }

    @Test
    @Transactional
    public void testCreateWithValidEventRegistrationURI() throws BusinessException, SQLException {
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
        remoteComServer.setEventRegistrationUri(EVENT_REGISTRATION_URI);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.save();
        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

        // Asserts
        assertEquals(EVENT_REGISTRATION_URI, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_INVALID_URL+"}", property = "eventRegistrationUri")
    public void testCreateWithInvalidEventRegistrationURI() throws BusinessException, SQLException {
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
        remoteComServer.setEventRegistrationUri(INVALID_URI);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.save();
        // Was expecting a BusinessException because the event registration URL is not valid
    }

    @Test
    @Transactional
    public void loadTest() throws BusinessException, SQLException {
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

        // Business method
        remoteComServer.save();
        RemoteComServer loadedRemoteComServer = (RemoteComServer) getEngineModelService().findComServer(remoteComServer.getId()).get();

        // asserts
        assertNotNull(loadedRemoteComServer);
        assertEquals(name, loadedRemoteComServer.getName());
        assertTrue("Was expecting the new com server to be active", loadedRemoteComServer.isActive());
        assertEquals(SERVER_LOG_LEVEL, loadedRemoteComServer.getServerLogLevel());
        assertEquals(COMMUNICATION_LOG_LEVEL, loadedRemoteComServer.getCommunicationLogLevel());
        assertEquals(CHANGES_INTER_POLL_DELAY, loadedRemoteComServer.getChangesInterPollDelay());
        assertEquals(SCHEDULING_INTER_POLL_DELAY, loadedRemoteComServer.getSchedulingInterPollDelay());
        assertNotNull("The OnlineComServer should not be null", loadedRemoteComServer.getOnlineComServer());
        assertEquals(onlineComServer.getId(), loadedRemoteComServer.getOnlineComServer().getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "onlineComServer")
    public void testCreateWithoutOnlineComServer() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        String name = "With-ComPort";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        remoteComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.save();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "serverLogLevel")
    @Transactional
    public void testCreateWithoutServerLogLevel() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        String name = "No-Server-LogLevel";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(null);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        remoteComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "communicationLogLevel")
    public void testCreateWithoutCommunicationLogLevel() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        String name = "No-Communication-LogLevel";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(null);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        remoteComServer.save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "changesInterPollDelay")
    public void testCreateWithoutChangesInterPollDelay() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        String name = "No-Changes-InterpollDelay";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(null);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        remoteComServer.save();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "schedulingInterPollDelay")
    @Transactional
    public void testCreateWithoutSchedulingInterPollDelay() throws BusinessException, SQLException {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        String name = "No-Scheduling-InterpollDelay";
        remoteComServer.setName(name);
        remoteComServer.setActive(true);
        remoteComServer.setServerLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setSchedulingInterPollDelay(null);
        remoteComServer.setOnlineComServer(createOnlineComServer());
        remoteComServer.save();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName(NO_VIOLATIONS_NAME);
        remoteComServer.setActive(false);
        remoteComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.setOnlineComServer(onlineComServer);

        remoteComServer.save();
        RemoteComServer duplicateComServer = getEngineModelService().newRemoteComServerInstance();
        duplicateComServer.setName(NO_VIOLATIONS_NAME);
        duplicateComServer.setActive(false);
        duplicateComServer.setServerLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        duplicateComServer.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        duplicateComServer.setOnlineComServer(onlineComServer);

        duplicateComServer.save();
    }

    @Test
    @Transactional
    public void testUpdate() throws BusinessException, SQLException {
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

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

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
        comServer.save();

        // Asserts
        assertEquals(changedName, comServer.getName());
        assertFalse("Was NOT expecting the com server to be active after update", comServer.isActive());
        assertEquals(changedServerLogLevel, comServer.getServerLogLevel());
        assertEquals(changedComLogLevel, comServer.getCommunicationLogLevel());
        assertEquals(changedChangesInterPollDelay, comServer.getChangesInterPollDelay());
        assertEquals(changedSchedulingInterPollDelay, comServer.getSchedulingInterPollDelay());
    }

    @Test
    @Transactional
    public void testResetEventRegistrationAPIViaBooleanMethod() throws BusinessException, SQLException {
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

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        // Business method
        comServer.setUsesDefaultEventRegistrationUri(true);
        comServer.save();

        // Asserts
        assertTrue(comServer.usesDefaultEventRegistrationUri());
        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    public void testResetEventRegistrationAPIViaNullURI() throws BusinessException, SQLException {
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

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        // Business method
        comServer.setEventRegistrationUri(null);
        comServer.save();

        // Asserts
        assertTrue(comServer.usesDefaultEventRegistrationUri());
        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "onlineComServer")
    public void testUpdateWithoutOnlineComServer() throws BusinessException, SQLException {
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

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

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

    private OnlineComServer createOnlineComServer() {
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