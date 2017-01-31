/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.ComPortType;

import java.text.MessageFormat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest extends PersistenceTest {

    private static final int QUERY_API_PORT = 123;
    private static final int STATUS_API_PORT = 456;
    private static final int STATUS_PORT = 8080;
    private static final int EVENT_REGISTRATION_PORT = 789;

    public static final TimeDuration ONE_MINUTE = new TimeDuration(60);
    public static final TimeDuration ONE_AND_A_HALF_MINUTE = new TimeDuration(90);
    public static final TimeDuration TWO_MINUTES = new TimeDuration(120);
    public static final TimeDuration FIVE_MINUTES = new TimeDuration(300);
    public static final TimeDuration TEN_MINUTES = new TimeDuration(600);
    public static final TimeDuration FIFTEEN_MINUTES = new TimeDuration(900);
    private static final String SERVER_NAME = "Onliner-2";

    @Test
    @Transactional
    public void testCreateLoadOfflineComServer() throws Exception {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "Offline";
        offlineComServer.name(name);
        offlineComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        offlineComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        offlineComServer.changesInterPollDelay(TEN_MINUTES);
        offlineComServer.schedulingInterPollDelay(FIFTEEN_MINUTES);
        offlineComServer.active(false);
        offlineComServer.create();

        OfflineComServer foundAfterCreate = (OfflineComServer) getEngineModelService().findComServer(name).get();
        assertThat(foundAfterCreate).isInstanceOf(OfflineComServer.class);
        assertThat(foundAfterCreate.getChangesInterPollDelay()).isEqualTo(TEN_MINUTES);
        assertThat(foundAfterCreate.getSchedulingInterPollDelay()).isEqualTo(FIFTEEN_MINUTES);
        assertThat(foundAfterCreate.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(foundAfterCreate.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(foundAfterCreate.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testFindOfflineComServerWithCaseInsensitiveName() throws Exception {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        String name = "OFFLINE";
        offlineComServer.name(name);
        offlineComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        offlineComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        offlineComServer.changesInterPollDelay(TEN_MINUTES);
        offlineComServer.schedulingInterPollDelay(FIFTEEN_MINUTES);
        offlineComServer.active(false);
        offlineComServer.create();

        ComServer byLowerCaseName = getEngineModelService().findComServer(name.toLowerCase()).get();
        assertThat(byLowerCaseName).isNotNull();
        assertThat(byLowerCaseName.getName()).isEqualTo(name);
    }

    @Test
    @Transactional
    public void testCreateLoadRemoteComServer() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String onlineName = "Online4Remote";
        onlineComServerBuilder.name(onlineName);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServerBuilder.changesInterPollDelay(TWO_MINUTES);
        onlineComServerBuilder.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServerBuilder.active(false);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.serverName(onlineName);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComserver = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "Remote";
        remoteComServer.name(name);
        remoteComServer.serverLogLevel(ComServer.LogLevel.WARN);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.changesInterPollDelay(ONE_MINUTE);
        remoteComServer.schedulingInterPollDelay(ONE_AND_A_HALF_MINUTE);
        remoteComServer.active(false);
        remoteComServer.onlineComServer(onlineComserver);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();

        ComServer reloadedRemoteComServer = getEngineModelService().findComServer(name).get();
        assertThat(reloadedRemoteComServer).isInstanceOf(RemoteComServer.class);
        assertThat(reloadedRemoteComServer.getChangesInterPollDelay()).isEqualTo(ONE_MINUTE);
        assertThat(reloadedRemoteComServer.getSchedulingInterPollDelay()).isEqualTo(ONE_AND_A_HALF_MINUTE);
        assertThat(reloadedRemoteComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(reloadedRemoteComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.TRACE);
        assertThat(reloadedRemoteComServer.isActive()).isEqualTo(false);
        assertThat(((RemoteComServer) reloadedRemoteComServer).getOnlineComServer().getName()).isEqualTo(onlineName);
    }

    @Test
    @Transactional
    public void testFindRemoteComServerWithCaseInsensitivName() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String onlineName = "Online4Remote";
        onlineComServerBuilder.name(onlineName);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServerBuilder.changesInterPollDelay(TWO_MINUTES);
        onlineComServerBuilder.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServerBuilder.active(false);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.serverName(onlineName);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();

        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        String name = "REMOTE";
        remoteComServer.name(name);
        remoteComServer.serverLogLevel(ComServer.LogLevel.WARN);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.changesInterPollDelay(ONE_MINUTE);
        remoteComServer.schedulingInterPollDelay(ONE_AND_A_HALF_MINUTE);
        remoteComServer.active(false);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        remoteComServer.create();

        ComServer reloadedRemoteComServer = getEngineModelService().findComServer(name.toLowerCase()).get();
        assertThat(reloadedRemoteComServer).isInstanceOf(RemoteComServer.class);
        assertThat(reloadedRemoteComServer.getName()).isEqualTo(name);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithUris() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        String name = "Onliner-2";
        onlineComServer.name(name);
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.changesInterPollDelay(TWO_MINUTES);
        onlineComServer.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServer.active(false);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.serverName(name);
        onlineComServer.queryApiPort(QUERY_API_PORT);
        onlineComServer.eventRegistrationPort(EVENT_REGISTRATION_PORT);

        onlineComServer.create();

        ComServer reloaded = getEngineModelService().findComServer(SERVER_NAME).get();
        assertThat(reloaded).isInstanceOf(OnlineComServer.class);
        assertThat(reloaded.getChangesInterPollDelay()).isEqualTo(TWO_MINUTES);
        assertThat(reloaded.getSchedulingInterPollDelay()).isEqualTo(FIVE_MINUTES);
        assertThat(reloaded.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(reloaded.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) reloaded).getQueryApiPort()).isEqualTo(QUERY_API_PORT);
        assertThat(((OnlineComServer) reloaded).getQueryApiPostUri()).isEqualTo(MessageFormat.format(ComServer.QUERY_API_URI_PATTERN, SERVER_NAME, QUERY_API_PORT));
        assertThat(((OnlineComServer) reloaded).getEventRegistrationPort()).isEqualTo(EVENT_REGISTRATION_PORT);
        assertThat(((OnlineComServer) reloaded).getEventRegistrationUri()).isEqualTo(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, SERVER_NAME, EVENT_REGISTRATION_PORT));
        assertThat(((OnlineComServer) reloaded).getStatusPort()).isEqualTo(STATUS_PORT);
        assertThat(((OnlineComServer) reloaded).getStatusUri()).isEqualTo(MessageFormat.format(ComServer.STATUS_URI_ATTERN, SERVER_NAME, Integer.toString(STATUS_PORT)));
        assertThat(((OnlineComServer) reloaded).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) reloaded).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) reloaded).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(reloaded.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithComPort() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String name = "Onliner-3";
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServerBuilder.changesInterPollDelay(TWO_MINUTES);
        onlineComServerBuilder.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServerBuilder.active(false);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        onlineComServer.newOutboundComPort("some comport", 4).comPortType(ComPortType.TCP).add();

        ComServer reloaded = getEngineModelService().findComServer(name).get();
        assertThat(reloaded).isInstanceOf(OnlineComServer.class);
        assertThat(reloaded.getComPorts()).hasSize(1);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}", property = "name")
    public void testCreateOnlineComServerWithEmptyName() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("");
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.changesInterPollDelay(TWO_MINUTES);
        onlineComServer.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServer.active(false);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.serverName("serverName");
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        onlineComServer.create();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.COMSERVER_NAME_INVALID_CHARS + "}", property = "name")
    public void testCreateOnlineComServerWithWrongName() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("%^&)(");
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.changesInterPollDelay(TWO_MINUTES);
        onlineComServer.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServer.active(false);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.serverName("serverName");
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        onlineComServer.create();
    }
}