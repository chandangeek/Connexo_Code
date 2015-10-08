package com.energyict.mdc.engine.config;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.energyict.mdc.engine.config.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.ComPortType;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest extends PersistenceTest {

    private static final String CUSTOM_EVENT_REGISTRATION_URI = "ws://another.domain.com/uri";
    private static final String CUSTOM_QUERY_API_URI = "ws://some.domain.com/uri";
    public static final TimeDuration ONE_MINUTE = new TimeDuration(60);
    public static final TimeDuration ONE_AND_A_HALF_MINUTE = new TimeDuration(90);
    public static final TimeDuration TWO_MINUTES = new TimeDuration(120);
    public static final TimeDuration FIVE_MINUTES = new TimeDuration(300);
    public static final TimeDuration TEN_MINUTES = new TimeDuration(600);
    public static final TimeDuration FIFTEEN_MINUTES = new TimeDuration(900);

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
        onlineComServerBuilder.usesDefaultQueryApiPostUri(true);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.usesDefaultEventRegistrationUri(true);
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
        onlineComServerBuilder.usesDefaultQueryApiPostUri(true);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.usesDefaultEventRegistrationUri(true);
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
        remoteComServer.create();

        ComServer reloadedRemoteComServer = getEngineModelService().findComServer(name.toLowerCase()).get();
        assertThat(reloadedRemoteComServer).isInstanceOf(RemoteComServer.class);
        assertThat(reloadedRemoteComServer.getName()).isEqualTo(name);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithDefaultUris() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("Onliner");
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.changesInterPollDelay(TWO_MINUTES);
        onlineComServer.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServer.active(false);
        onlineComServer.usesDefaultQueryApiPostUri(true);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.usesDefaultEventRegistrationUri(true);

        onlineComServer.create();

        ComServer comServer = getEngineModelService().findComServer("Onliner").get();
        assertThat(comServer).isInstanceOf(OnlineComServer.class);
        OnlineComServer foundAfterCreate = (OnlineComServer) comServer;
        assertThat(foundAfterCreate.getChangesInterPollDelay()).isEqualTo(TWO_MINUTES);
        assertThat(foundAfterCreate.getSchedulingInterPollDelay()).isEqualTo(FIVE_MINUTES);
        assertThat(foundAfterCreate.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(foundAfterCreate.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(foundAfterCreate.usesDefaultQueryApiPostUri()).isEqualTo(true);
        assertThat(foundAfterCreate.getQueryApiPostUri()).isEqualTo("ws://Onliner:8889/remote/queries");
        assertThat(foundAfterCreate.usesDefaultEventRegistrationUri()).isEqualTo(true);
        assertThat(foundAfterCreate.getEventRegistrationUri()).isEqualTo("ws://Onliner:8888/events/registration");
        assertThat(foundAfterCreate.getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(foundAfterCreate.getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(foundAfterCreate.getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(foundAfterCreate.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithCustomUris() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.name("Onliner-2");
        onlineComServer.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.changesInterPollDelay(TWO_MINUTES);
        onlineComServer.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServer.active(false);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.eventRegistrationUri(CUSTOM_EVENT_REGISTRATION_URI);
        onlineComServer.queryApiPostUri(CUSTOM_QUERY_API_URI);

        onlineComServer.create();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-2").get();
        assertThat(reloaded).isInstanceOf(OnlineComServer.class);
        assertThat(reloaded.getChangesInterPollDelay()).isEqualTo(TWO_MINUTES);
        assertThat(reloaded.getSchedulingInterPollDelay()).isEqualTo(FIVE_MINUTES);
        assertThat(reloaded.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(reloaded.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) reloaded).usesDefaultQueryApiPostUri()).isEqualTo(false);
        assertThat(((OnlineComServer) reloaded).getQueryApiPostUri()).isEqualTo(CUSTOM_QUERY_API_URI);
        assertThat(((OnlineComServer) reloaded).usesDefaultEventRegistrationUri()).isEqualTo(false);
        assertThat(((OnlineComServer) reloaded).getEventRegistrationUri()).isEqualTo(CUSTOM_EVENT_REGISTRATION_URI);
        assertThat(((OnlineComServer) reloaded).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) reloaded).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) reloaded).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(reloaded.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithComPort() throws Exception {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        onlineComServerBuilder.name("Onliner-3");
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServerBuilder.changesInterPollDelay(TWO_MINUTES);
        onlineComServerBuilder.schedulingInterPollDelay(FIVE_MINUTES);
        onlineComServerBuilder.active(false);
        onlineComServerBuilder.usesDefaultQueryApiPostUri(true);
        onlineComServerBuilder.storeTaskQueueSize(10);
        onlineComServerBuilder.storeTaskThreadPriority(3);
        onlineComServerBuilder.numberOfStoreTaskThreads(6);
        onlineComServerBuilder.usesDefaultEventRegistrationUri(true);
        final OnlineComServer onlineComServer = onlineComServerBuilder.create();
        onlineComServer.newOutboundComPort("some comport", 4).comPortType(ComPortType.TCP).add();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-3").get();
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
        onlineComServer.usesDefaultQueryApiPostUri(true);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.usesDefaultEventRegistrationUri(true);
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
        onlineComServer.usesDefaultQueryApiPostUri(true);
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.usesDefaultEventRegistrationUri(true);
        onlineComServer.create();
    }
}