package com.energyict.mdc.engine.model;

import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest extends PersistenceTest {
    @Test
    public void testCreateLoadOfflineComServer() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
            offlineComServer.setName("Offliner");
            offlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
            offlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
            offlineComServer.setChangesInterPollDelay(new TimeDuration(600));
            offlineComServer.setSchedulingInterPollDelay(new TimeDuration(900));
            offlineComServer.setActive(false);
            offlineComServer.save();
            context.commit();
        }

        ComServer offlineComServer = getEngineModelService().findComServer("Offliner");
        assertTrue(offlineComServer instanceof OfflineComServer);
        assertThat(offlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(600));
        assertThat(offlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(900));
        assertThat(offlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.ERROR);
        assertThat(offlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(offlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadRemoteComServer() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
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
            context.commit();
        }

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
    public void testCreateLoadOnlineComServerWithDefaultUris() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
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
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) onlineComServer).usesDefaultQueryApiPostUri()).isEqualTo(true);
        assertThat(((OnlineComServer) onlineComServer).getQueryApiPostUri()).isEqualTo("http://Onliner:8889/remote/queries");
        assertThat(((OnlineComServer) onlineComServer).usesDefaultEventRegistrationUri()).isEqualTo(true);
        assertThat(((OnlineComServer) onlineComServer).getEventRegistrationUri()).isEqualTo("ws://Onliner:8888/events/registration");
        assertThat(((OnlineComServer) onlineComServer).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(onlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadOnlineComServerWithCustomUris() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
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
            onlineComServer.setEventRegistrationUri("/some/uri");
            onlineComServer.setQueryAPIPostUri("/another/uri");

            onlineComServer.save();
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner-2");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getChangesInterPollDelay()).isEqualTo(new TimeDuration(120));
        assertThat(onlineComServer.getSchedulingInterPollDelay()).isEqualTo(new TimeDuration(300));
        assertThat(onlineComServer.getServerLogLevel()).isEqualTo(ComServer.LogLevel.DEBUG);
        assertThat(onlineComServer.getCommunicationLogLevel()).isEqualTo(ComServer.LogLevel.INFO);
        assertThat(((OnlineComServer) onlineComServer).usesDefaultQueryApiPostUri()).isEqualTo(false);
        assertThat(((OnlineComServer) onlineComServer).getQueryApiPostUri()).isEqualTo("/another/uri");
        assertThat(((OnlineComServer) onlineComServer).usesDefaultEventRegistrationUri()).isEqualTo(false);
        assertThat(((OnlineComServer) onlineComServer).getEventRegistrationUri()).isEqualTo("/some/uri");
        assertThat(((OnlineComServer) onlineComServer).getNumberOfStoreTaskThreads()).isEqualTo(6);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskThreadPriority()).isEqualTo(3);
        assertThat(((OnlineComServer) onlineComServer).getStoreTaskQueueSize()).isEqualTo(10);
        assertThat(onlineComServer.isActive()).isEqualTo(false);
    }

    @Test
    public void testCreateLoadOnlineComServerWithComPort() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
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
            context.commit();
        }

        ComServer onlineComServer = getEngineModelService().findComServer("Onliner-3");
        assertTrue(onlineComServer instanceof OnlineComServer);
        assertThat(onlineComServer.getComPorts()).hasSize(1);
    }

}
