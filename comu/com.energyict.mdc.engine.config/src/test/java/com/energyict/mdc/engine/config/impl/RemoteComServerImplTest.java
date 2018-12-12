/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.RemoteComServer;

import com.google.inject.Provider;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    private static final TimeDuration CHANGES_INTER_POLL_DELAY = new TimeDuration(5, TimeDuration.TimeUnit.HOURS);
    private static final TimeDuration SCHEDULING_INTER_POLL_DELAY = new TimeDuration(2, TimeDuration.TimeUnit.MINUTES);
    private static final int INVALID_EVENT_REGISTRATION_PORT = 100000;

    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPortImpl> outboundComPortProvider;

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws SQLException {
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
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS +"}", property = "name")
    @Transactional
    public void testNameWithInvalidCharacters() throws SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "Read my lips: no spaces or special chars like ? or !, not to mention / or @";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "changesInterPollDelay")
    public void testTooSmallChangesInterPollDelay() throws SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "testTooSmallChangesInterPollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "schedulingInterPollDelay")
    public void testTooSmallSchedulingInterPollDelay() throws SQLException {
        OnlineComServer onlineComServer = this.createOnlineComServer();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "testTooSmallSchedulingInterPollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServer.create();
    }

    @Test
    @Transactional
    public void testCreateWithValidEventRegistrationPort() throws SQLException {
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
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        // Business method
        remoteComServer.create();
        RemoteComServer comServer = (RemoteComServer) getEngineModelService().findComServer(name).get();

        // Asserts
        assertEquals(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER, comServer.getEventRegistrationPort());
        assertEquals(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, name, Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)), comServer.getEventRegistrationUri());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "eventRegistrationPort")
    public void testCreateWithInvalidEventRegistrationPort() throws SQLException {
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
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(INVALID_EVENT_REGISTRATION_PORT);

        // Business method

        remoteComServer.create();
        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void loadTest() throws SQLException {
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
        remoteComServerBuilder.serverName(name);
        remoteComServerBuilder.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

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
    public void testCreateWithoutOnlineComServer() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "With-ComPort";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    public void testCreateWithoutName() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName("remoteServerName");
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        // Business method
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "serverLogLevel")
    @Transactional
    public void testCreateWithoutServerLogLevel() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Server-LogLevel";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(null);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "communicationLogLevel")
    public void testCreateWithoutCommunicationLogLevel() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Communication-LogLevel";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(null);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "changesInterPollDelay")
    public void testCreateWithoutChangesInterPollDelay() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Changes-InterpollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(null);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "schedulingInterPollDelay")
    @Transactional
    public void testCreateWithoutSchedulingInterPollDelay() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "No-Scheduling-InterpollDelay";
        remoteComServer.name(name);
        remoteComServer.active(true);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(null);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name(NO_VIOLATIONS_NAME);
        remoteComServer.active(false);
        remoteComServer.serverLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(NO_VIOLATIONS_NAME);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        remoteComServer.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> duplicateComServer = getEngineModelService().newRemoteComServerBuilder();
        duplicateComServer.name(NO_VIOLATIONS_NAME);
        duplicateComServer.active(false);
        duplicateComServer.serverLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        duplicateComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        duplicateComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        duplicateComServer.onlineComServer(onlineComServer);
        duplicateComServer.serverName(NO_VIOLATIONS_NAME);
        duplicateComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        duplicateComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);

        duplicateComServer.create();
    }

    @Test
    @Transactional
    public void testUpdate() throws SQLException {
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
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
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
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "onlineComServer")
    public void testUpdateWithoutOnlineComServer() throws SQLException {
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
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
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

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "serverName")
    public void testCreateWithoutServerName() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.active(true);
        remoteComServer.name("remoteName");
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());

        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        // Business method
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "statusPort")
    public void testCreateWithoutStatusPort() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "remoteName";
        remoteComServer.active(true);
        remoteComServer.name(name);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        // Business method
        remoteComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_VALUE_NOT_IN_RANGE + "}", property = "eventRegistrationPort")
    public void testCreateWithoutEventRegistrationPort() throws SQLException {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "remoteName";
        remoteComServer.active(true);
        remoteComServer.name(name);
        remoteComServer.serverLogLevel(SERVER_LOG_LEVEL);
        remoteComServer.communicationLogLevel(COMMUNICATION_LOG_LEVEL);
        remoteComServer.changesInterPollDelay(CHANGES_INTER_POLL_DELAY);
        remoteComServer.schedulingInterPollDelay(SCHEDULING_INTER_POLL_DELAY);
        remoteComServer.onlineComServer(createOnlineComServer());
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        // Business method
        remoteComServer.create();
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
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServerBuilder.create();
    }

}