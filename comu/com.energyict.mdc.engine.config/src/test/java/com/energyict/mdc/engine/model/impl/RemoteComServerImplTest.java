//package com.energyict.mdc.engine.model.impl;
//
//import com.elster.jupiter.orm.DataModel;
//import com.energyict.mdc.common.BusinessException;
//import com.energyict.mdc.common.InvalidValueException;
//import com.energyict.mdc.common.TimeDuration;
//import com.energyict.mdc.engine.model.ComServer;
//import com.energyict.mdc.engine.model.EngineModelService;
//import com.energyict.mdc.engine.model.OnlineComServer;
//import com.energyict.mdc.engine.model.PersistenceTest;
//import com.energyict.mdc.engine.model.RemoteComServer;
//import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
//import com.energyict.mdc.shadow.servers.RemoteComServerShadow;
//import java.sql.SQLException;
//import org.junit.Test;
//import org.mockito.Mock;
//
//import static org.fest.assertions.api.Assertions.assertThat;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
///**
// * Tests the {@link RemoteComServerImpl} component.
// *
// * @author Rudi Vankeirsbilck (rudi)
// * @since 2012-04-18 (16:52)
// */
//public class RemoteComServerImplTest extends PersistenceTest {
//
//    private static int onlineNameNumber = 1;
//
//    private static final String NO_VIOLATIONS_NAME = "Remote-No-Violations";
//    private static final ComServer.LogLevel SERVER_LOG_LEVEL = ComServer.LogLevel.ERROR;
//    private static final ComServer.LogLevel COMMUNICATION_LOG_LEVEL = ComServer.LogLevel.TRACE;
//    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.HOURS);
//    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.MINUTES);
//    private static final String QUERY_API_USER_NAME = "johndoe";
//    private static final String QUERY_API_PASSWORD = "doe";
//    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/custom/events/registration";
//    private static final String INVALID_URI = "Anything but a valid URI";
//
//    @Mock
//    EngineModelService engineModelService;
//    @Mock
//    DataModel dataModel;
//
//    @Test
//    public void testGetTypeDoesNotReturnServerBasedClassName() {
//        RemoteComServer onlineComServer = new RemoteComServerImpl(dataModel, engineModelService);
//
//        // Business method
//        String type = onlineComServer.getType();
//
//        // Asserts
//        Assertions.assertThat(type).doesNotContain(".Server");
//    }
//
//    @Test
//    public void testCreateWithoutViolations() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = NO_VIOLATIONS_NAME;
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        // Business method
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Asserts
//        assertEquals(name, comServer.getName());
//        assertTrue("Was expecting the new com server to be active", comServer.isActive());
//        assertEquals(SERVER_LOG_LEVEL, comServer.getServerLogLevel());
//        assertEquals(COMMUNICATION_LOG_LEVEL, comServer.getCommunicationLogLevel());
//        assertEquals(CHANGES_INTER_POLL_DELAY, comServer.getChangesInterPollDelay());
//        assertEquals(SCHEDULING_INTER_POLL_DELAY, comServer.getSchedulingInterPollDelay());
//        assertEquals(QUERY_API_USER_NAME, comServer.getQueryAPIUsername());
//        assertEquals(QUERY_API_PASSWORD, comServer.getQueryAPIPassword());
//        assertEquals(onlineComServer.getId(), comServer.getOnlineComServer().getId());
//    }
//
//    @Test
//    public void testThatDefaultURIsAreApplied() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = NO_VIOLATIONS_NAME;
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        // Business method
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Asserts
//        assertTrue(comServer.usesDefaultEventRegistrationUri());
//        assertNotNull(comServer.getEventRegistrationUri());
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testNameWithInvalidCharacters() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        shadow.setName("Read my lips: no spaces or special chars like ? or !, not to mention / or @");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        try {
//            // Business method
//            new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//        } catch (BusinessException e) {
//            // Asserts
//            assertEquals("nameXcontainsInvalidChars", e.getMessageId());
//            throw e;
//        }
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testTooSmallChangesInterPollDelay() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        shadow.setName("testTooSmallChangesInterPollDelay");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        try {
//            // Business method
//            new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//        } catch (BusinessException e) {
//            // Asserts
//            assertEquals("XshouldBeAtLeast", e.getMessageId());
//            throw e;
//        }
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testTooSmallSchedulingInterPollDelay() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        shadow.setName("testTooSmallSchedulingInterPollDelay");
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(new TimeDuration(1, TimeDuration.SECONDS));
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        try {
//            // Business method
//            new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//        } catch (BusinessException e) {
//            // Asserts
//            assertEquals("XshouldBeAtLeast", e.getMessageId());
//            throw e;
//        }
//    }
//
//    @Test
//    public void testCreateWithValidEventRegistrationURI() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = NO_VIOLATIONS_NAME;
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//        shadow.setEventRegistrationUri(EVENT_REGISTRATION_URI);
//
//        // Business method
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Asserts
//        assertEquals(EVENT_REGISTRATION_URI, comServer.getEventRegistrationUri());
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testCreateWithInvalidEventRegistrationURI() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = NO_VIOLATIONS_NAME;
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//        shadow.setEventRegistrationUri(INVALID_URI);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Was expecting a BusinessException because the event registration URL is not valid
//    }
//
//    @Test
//    public void loadTest() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = NO_VIOLATIONS_NAME;
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setOnlineComServerId((int) onlineComServer.getId());
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        // Business method
//        RemoteComServer createdComServer = new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//        RemoteComServer loadedRemoteComServer = (RemoteComServer) new ComServerFactoryImpl(engineModelService).find((int) createdComServer.getId());
//
//        // asserts
//        assertNotNull(loadedRemoteComServer);
//        assertEquals(name, loadedRemoteComServer.getName());
//        assertTrue("Was expecting the new com server to be active", loadedRemoteComServer.isActive());
//        assertEquals(SERVER_LOG_LEVEL, loadedRemoteComServer.getServerLogLevel());
//        assertEquals(COMMUNICATION_LOG_LEVEL, loadedRemoteComServer.getCommunicationLogLevel());
//        assertEquals(CHANGES_INTER_POLL_DELAY, loadedRemoteComServer.getChangesInterPollDelay());
//        assertEquals(SCHEDULING_INTER_POLL_DELAY, loadedRemoteComServer.getSchedulingInterPollDelay());
//        assertEquals(QUERY_API_USER_NAME, loadedRemoteComServer.getQueryAPIUsername());
//        assertEquals(QUERY_API_PASSWORD, loadedRemoteComServer.getQueryAPIPassword());
//        assertNotNull("The OnlineComServer should not be null", loadedRemoteComServer.getOnlineComServer());
//        assertEquals(onlineComServer.getId(), loadedRemoteComServer.getOnlineComServer().getId());
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutOnlineComServer() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = "With-ComPort";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setQueryAPIUsername(QUERY_API_USER_NAME);
//        shadow.setQueryAPIPassword(QUERY_API_PASSWORD);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting an InvalidValueException
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testCreateWithoutName() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because the name is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutServerLogLevel() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = "No-Server-LogLevel";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(null);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because the server log level is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutCommunicationLogLevel() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = "No-Communication-LogLevel";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(null);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because the communication log level is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutChangesInterPollDelay() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = "No-Changes-InterpollDelay";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(null);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because the changes interpoll delay is not set
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testCreateWithoutSchedulingInterPollDelay() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        String name = "No-Scheduling-InterpollDelay";
//        shadow.setName(name);
//        shadow.setActive(true);
//        shadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(null);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because the scheduling interpoll delay is not set
//    }
//
//    @Test(expected = BusinessException.class)
//    public void testCreateWithExistingName() throws BusinessException, SQLException {
//        RemoteComServerShadow shadow = new RemoteComServerShadow();
//        shadow.setName(NO_VIOLATIONS_NAME);
//        shadow.setActive(false);
//        shadow.setServerLogLevel(ComServer.LogLevel.TRACE);
//        shadow.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        shadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//
//        // Business method
//        new ComServerFactoryImpl(engineModelService).createRemote(shadow);
//
//        // Expecting a BusinessException to be thrown because a ComServer with the same name already exists
//    }
//
//    @Test
//    public void testUpdate() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow creationShadow = new RemoteComServerShadow();
//        String name = "Update-Candidate";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setOnlineComServerId((int) onlineComServer.getId());
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URI);
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(creationShadow);
//
//        // Business method
//        String changedName = "Name-Updated";
//        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
//        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
//        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
//        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
//        comServer.setName(changedName);
//        comServer.setActive(false);
//        comServer.setServerLogLevel(changedServerLogLevel);
//        comServer.setCommunicationLogLevel(changedComLogLevel);
//        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
//        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
//        comServer.setQueryAPIUsername(QUERY_API_USER_NAME);
//        comServer.setQueryAPIPassword(QUERY_API_PASSWORD);
//        comServer.save();
//
//        // Asserts
//        assertEquals(changedName, comServer.getName());
//        assertFalse("Was NOT expecting the com server to be active after update", comServer.isActive());
//        assertEquals(changedServerLogLevel, comServer.getServerLogLevel());
//        assertEquals(changedComLogLevel, comServer.getCommunicationLogLevel());
//        assertEquals(changedChangesInterPollDelay, comServer.getChangesInterPollDelay());
//        assertEquals(changedSchedulingInterPollDelay, comServer.getSchedulingInterPollDelay());
//        assertEquals(QUERY_API_USER_NAME, comServer.getQueryAPIUsername());
//        assertEquals(QUERY_API_PASSWORD, comServer.getQueryAPIPassword());
//    }
//
//    @Test
//    public void testResetEventRegistrationAPIViaBooleanMethod() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow creationShadow = new RemoteComServerShadow();
//        String name = "withCustomURI";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setOnlineComServerId((int) onlineComServer.getId());
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URI);
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(creationShadow);
//        String eventRegistrationUri = comServer.getEventRegistrationUri();
//
//        // Business method
//        comServer.setUsesDefaultEventRegistrationUri(true);
//        comServer.save();
//
//        // Asserts
//        assertTrue(comServer.usesDefaultEventRegistrationUri());
//        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
//    }
//
//    @Test
//    public void testResetEventRegistrationAPIViaNullURI() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow creationShadow = new RemoteComServerShadow();
//        String name = "withCustomURI";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setOnlineComServerId((int) onlineComServer.getId());
//        creationShadow.setEventRegistrationUri(EVENT_REGISTRATION_URI);
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(creationShadow);
//        String eventRegistrationUri = comServer.getEventRegistrationUri();
//
//        // Business method
//        comServer.setEventRegistrationUri(null);
//        comServer.save();
//
//        // Asserts
//        assertTrue(comServer.usesDefaultEventRegistrationUri());
//        assertNotEquals(eventRegistrationUri, comServer.getEventRegistrationUri());
//    }
//
//    @Test(expected = InvalidValueException.class)
//    public void testUpdateWithoutOnlineComServer() throws BusinessException, SQLException {
//        OnlineComServer onlineComServer = this.createOnlineComServer();
//
//        RemoteComServerShadow creationShadow = new RemoteComServerShadow();
//        String name = "Update-Candidate2";
//        creationShadow.setName(name);
//        creationShadow.setActive(true);
//        creationShadow.setServerLogLevel(SERVER_LOG_LEVEL);
//        creationShadow.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        creationShadow.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        creationShadow.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        creationShadow.setOnlineComServerId((int) onlineComServer.getId());
//        RemoteComServer comServer = new ComServerFactoryImpl(engineModelService).createRemote(creationShadow);
//
//        // Business method
//        String changedName = "Name-Updated2";
//        ComServer.LogLevel changedServerLogLevel = ComServer.LogLevel.WARN;
//        ComServer.LogLevel changedComLogLevel = ComServer.LogLevel.INFO;
//        TimeDuration changedChangesInterPollDelay = SCHEDULING_INTER_POLL_DELAY;
//        TimeDuration changedSchedulingInterPollDelay = CHANGES_INTER_POLL_DELAY;
//        comServer.setName(changedName);
//        comServer.setActive(false);
//        comServer.setServerLogLevel(changedServerLogLevel);
//        comServer.setCommunicationLogLevel(changedComLogLevel);
//        comServer.setChangesInterPollDelay(changedChangesInterPollDelay);
//        comServer.setSchedulingInterPollDelay(changedSchedulingInterPollDelay);
//        comServer.setOnlineComServer(null);
//        comServer.save();
//
//        // Expecting an InvalidValueException
//    }
//
//    private OnlineComServer createOnlineComServer() throws BusinessException, SQLException {
//        OnlineComServerShadow shadow1 = new OnlineComServerShadow();
//        String name = "Online-" + onlineNameNumber++;
//        shadow1.setName(name);
//        shadow1.setActive(true);
//        shadow1.setServerLogLevel(SERVER_LOG_LEVEL);
//        shadow1.setCommunicationLogLevel(COMMUNICATION_LOG_LEVEL);
//        shadow1.setChangesInterPollDelay(CHANGES_INTER_POLL_DELAY);
//        shadow1.setSchedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
//        OnlineComServerShadow shadow = shadow1;
//        return new ComServerFactoryImpl(engineModelService).createOnline(shadow);
//    }
//
//}