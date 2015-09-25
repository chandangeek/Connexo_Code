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

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = NO_VIOLATIONS_NAME;
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);

        // Business method
        remoteComServer.create();
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

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = NO_VIOLATIONS_NAME;
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);

        // Business method
        remoteComServer.create();
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

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name("testTooSmallChangesInterPollDelay");
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name("testTooSmallSchedulingInterPollDelay");
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.onlineComServer(onlineComServer);

        remoteComServer.create();
    }

    @Test
    @Transactional
    public void testCreateWithValidEventRegistrationURI() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = NO_VIOLATIONS_NAME;
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        remoteComServer.onlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.create();
        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

        // Asserts
        assertEquals(EVENT_REGISTRATION_URI, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_INVALID_URL+"}", property = "eventRegistrationUri")
    public void testCreateWithInvalidEventRegistrationURI() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = NO_VIOLATIONS_NAME;
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.eventRegistrationUri(INVALID_URI);
        remoteComServer.onlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.create();
        // Was expecting a BusinessException because the event registration URL is not valid
    }

    @Test
    @Transactional
    public void loadTest() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServerBuilder = getEngineModelService().newRemoteComServerBuilder();
        String name = NO_VIOLATIONS_NAME;
        remoteComServerBuilder.name(name);
        remoteComServerBuilder.active(true);
        remoteComServerBuilder.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServerBuilder.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServerBuilder.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServerBuilder.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServerBuilder.onlineComServer(onlineComServer);

        // Business method
        final RemoteComServer remoteComServer = remoteComServerBuilder.create();
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
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "With-ComPort";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws BusinessException, SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        // Business method
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "serverLogLevel")
    @Transactional
    public void testCreateWithoutServerLogLevel() throws BusinessException, SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Server-LogLevel";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(null);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "communicationLogLevel")
    public void testCreateWithoutCommunicationLogLevel() throws BusinessException, SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Communication-LogLevel";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(null);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "changesInterPollDelay")
    public void testCreateWithoutChangesInterPollDelay() throws BusinessException, SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Changes-InterpollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(null);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "schedulingInterPollDelay")
    @Transactional
    public void testCreateWithoutSchedulingInterPollDelay() throws BusinessException, SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Scheduling-InterpollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(null);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name(NO_VIOLATIONS_NAME);
        remoteComServer.active(false);
        remoteComServer.serverLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);

        remoteComServer.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> duplicateComServer = getEngineModelService().newRemoteComServerBuilder();
        duplicateComServer.name(NO_VIOLATIONS_NAME);
        duplicateComServer.active(false);
        duplicateComServer.serverLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        duplicateComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        duplicateComServer.onlineComServer(onlineComServer);

        duplicateComServer.create();
    }

    @Test
    @Transactional
    public void testUpdate() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "Update-Candidate";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        remoteComServer.create();

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
        comServer.update();

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

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "withCustomURI";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        remoteComServer.create();

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        // Business method
        comServer.setUsesDefaultEventRegistrationUri(true);
        comServer.update();

        // Asserts
        assertTrue(comServer.usesDefaultEventRegistrationUri());
        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    public void testResetEventRegistrationAPIViaNullURI() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "withCustomURI";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.eventRegistrationUri(EVENT_REGISTRATION_URI);
        remoteComServer.create();

        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();
        String eventRegistrationUri = comServer.getEventRegistrationUri();

        // Business method
        comServer.setEventRegistrationUri(null);
        comServer.update();

        // Asserts
        assertTrue(comServer.usesDefaultEventRegistrationUri());
        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "onlineComServer")
    public void testUpdateWithoutOnlineComServer() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "Update-Candidate2";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.create();

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
        comServer.update();
    }

    private OnlineComServer createOnlineComServer() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String name = "Online-" + onlineNameNumber++;
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServerBuilder.changesInterPollDelay(new TimeDuration(60));
        onlineComServerBuilder.schedulingInterPollDelay(new TimeDuration(90));
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        return onlineComServerBuilder.create();
    }

}