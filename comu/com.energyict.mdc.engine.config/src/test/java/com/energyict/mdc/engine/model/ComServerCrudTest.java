package com.energyict.mdc.engine.model;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.google.common.base.Optional;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.ComPortType;

import java.net.URI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest extends PersistenceTest {

    private static final String CUSTOM_EVENT_REGISTRATION_URI = "ws://another.domain.com/uri";
    private static final String CUSTOM_QUERY_API_URI = "ws://some.domain.com/uri";

    @Test
    @Transactional
    public void testCreateLoadOfflineComServer() throws Exception {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName("Offliner");
        offlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        offlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        offlineComServer.setChangesInterPollDelay(new TimeDuration(600));
        offlineComServer.setSchedulingInterPollDelay(new TimeDuration(900));
        offlineComServer.setActive(false);
        offlineComServer.save();

        offlineComServer = (OfflineComServer) getEngineModelService().findComServer("Offliner").get();
        assertTrue(offlineComServer instanceof OfflineComServer);
        assertThat(offlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(600));
        assertThat(offlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(900));
        assertThat(offlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(offlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(offlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCreateLoadRemoteComServer() throws Exception {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName("Online4Remote");
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
        onlineComServer.setActive(false);
        onlineComServer.setUsesDefaultQueryAPIPostUri(true);
        onlineComServer.setStoreTaskQueueSize(10);
        onlineComServer.setStoreTaskThreadPriority(3);
        onlineComServer.setNumberOfStoreTaskThreads(6);
        onlineComServer.setUsesDefaultEventRegistrationUri(true);

        onlineComServer.save();

        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName("Remoter");
        remoteComServer.setServerLogLevel(ComServer.LogLevel.WARN);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        remoteComServer.setChangesInterPollDelay(new TimeDuration(60));
        remoteComServer.setSchedulingInterPollDelay(new TimeDuration(90));
        remoteComServer.setActive(false);
        Optional<ComServer> comServer = getEngineModelService().findComServer("Online4Remote");
        remoteComServer.setOnlineComServer((OnlineComServer) comServer.get());
        remoteComServer.save();

        ComServer reloadedRemoteComServer = getEngineModelService().findComServer("Remoter").get();
        assertTrue(reloadedRemoteComServer instanceof RemoteComServer);
        assertThat(reloadedRemoteComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(60));
        assertThat(reloadedRemoteComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(90));
        assertThat(reloadedRemoteComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(reloadedRemoteComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.TRACE);
        assertThat(reloadedRemoteComServer.isActive()).isEqualTo(false);
        assertThat(((RemoteComServer) reloadedRemoteComServer).getOnlineComServer().getName()).isEqualTo("Online4Remote");
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithDefaultUris() throws Exception {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName("Onliner");
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
        onlineComServer.setActive(false);
        onlineComServer.setUsesDefaultQueryAPIPostUri(true);
        onlineComServer.setStoreTaskQueueSize(10);
        onlineComServer.setStoreTaskThreadPriority(3);
        onlineComServer.setNumberOfStoreTaskThreads(6);
        onlineComServer.setUsesDefaultEventRegistrationUri(true);

        onlineComServer.save();

        ComServer comServer = getEngineModelService().findComServer("Onliner").get();
        assertTrue(comServer instanceof OnlineComServer);
        onlineComServer = (OnlineComServer) comServer;
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(onlineComServer.usesDefaultQueryApiPostUri()).isEqualTo(true);
        assertThat(onlineComServer.getQueryApiPostUri()).isEqualTo("ws://Onliner:8889/remote/queries");
        assertThat(onlineComServer.usesDefaultEventRegistrationUri()).isEqualTo(true);
        assertThat(onlineComServer.getEventRegistrationUri()).isEqualTo("ws://Onliner:8888/events/registration");
        assertThat(onlineComServer.getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(onlineComServer.getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(onlineComServer.getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(onlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCreateLoadOnlineComServerWithCustomUris() throws Exception {
        URI uri = new URI(CUSTOM_EVENT_REGISTRATION_URI);
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName("Onliner-2");
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
        onlineComServer.setActive(false);
        onlineComServer.setStoreTaskQueueSize(10);
        onlineComServer.setStoreTaskThreadPriority(3);
        onlineComServer.setNumberOfStoreTaskThreads(6);
        onlineComServer.setEventRegistrationUri(CUSTOM_EVENT_REGISTRATION_URI);
        onlineComServer.setQueryAPIPostUri(CUSTOM_QUERY_API_URI);

        onlineComServer.save();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-2").get();
        assertTrue(reloaded instanceof OnlineComServer);
        assertThat(reloaded.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(reloaded.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
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
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setName("Onliner-3");
        onlineComServer.setServerLogLevel(ComServer.LogLevel.DEBUG);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.INFO);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(120));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(300));
        onlineComServer.setActive(false);
        onlineComServer.setUsesDefaultQueryAPIPostUri(true);
        onlineComServer.setStoreTaskQueueSize(10);
        onlineComServer.setStoreTaskThreadPriority(3);
        onlineComServer.setNumberOfStoreTaskThreads(6);
        onlineComServer.setUsesDefaultEventRegistrationUri(true);
        onlineComServer.save();
        onlineComServer.newOutboundComPort("some comport", 4).comPortType(ComPortType.TCP).add();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-3").get();
        assertTrue(reloaded instanceof OnlineComServer);
        assertThat(reloaded.getComPorts()).hasSize(1);
    }

}
