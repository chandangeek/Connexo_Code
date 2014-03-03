package com.energyict.mdc.engine.model;

import com.energyict.mdc.Transactional;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.ComPortType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest extends PersistenceTest {
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

        offlineComServer = (OfflineComServer) getEngineModelService().findComServer("Offliner");
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
        remoteComServer.setOnlineComServer((OnlineComServer) getEngineModelService().findComServer("Online4Remote"));
        remoteComServer.save();

        ComServer offlineComServer = getEngineModelService().findComServer("Remoter");
        assertTrue(offlineComServer instanceof RemoteComServer);
        assertThat(offlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(60));
        assertThat(offlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(90));
        assertThat(offlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.WARN);
        assertThat(offlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.TRACE);
        assertThat(offlineComServer.isActive()).isEqualTo(false);
        assertThat(((RemoteComServer) offlineComServer).getOnlineComServer().getName()).isEqualTo("Online4Remote");
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

        onlineComServer = (OnlineComServer) getEngineModelService().findComServer("Onliner");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(onlineComServer.usesDefaultQueryApiPostUri()).isEqualTo(true);
        assertThat(onlineComServer.getQueryApiPostUri()).isEqualTo("http://Onliner:8889/remote/queries");
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
        onlineComServer.setEventRegistrationUri("http://some/uri");
        onlineComServer.setQueryAPIPostUri("http://another/uri");

        onlineComServer.save();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-2");
        assertTrue(reloaded instanceof OnlineComServer);
        assertThat(reloaded.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(reloaded.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(reloaded.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(reloaded.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) reloaded).usesDefaultQueryApiPostUri()).isEqualTo(false);
        assertThat(((OnlineComServer) reloaded).getQueryApiPostUri()).isEqualTo("http://another/uri");
        assertThat(((OnlineComServer) reloaded).usesDefaultEventRegistrationUri()).isEqualTo(false);
        assertThat(((OnlineComServer) reloaded).getEventRegistrationUri()).isEqualTo("http://some/uri");
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
        onlineComServer.newOutboundComPort().name("some comport").comPortType(ComPortType.TCP).numberOfSimultaneousConnections(4).add();

        ComServer reloaded = getEngineModelService().findComServer("Onliner-3");
        assertTrue(reloaded instanceof OnlineComServer);
        assertThat(reloaded.getComPorts()).hasSize(1);
    }

}
