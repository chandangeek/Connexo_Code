package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.ports.ComPortType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EngineConfigurationServiceImplTest extends PersistenceTest {

    @Test
    @Transactional
    public void testCanCreateComServerWithSameNameAsObsoleteComServer() throws Exception {
        OnlineComServer serverOne = createOnlineComServer("serverOne");
        serverOne.makeObsolete();
        createOnlineComServer("serverOne");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_SERVER+"}")
    public void testCanNotCreateComServerWithSameNameAsNonObsoleteComServer() throws Exception {
        createOnlineComServer("serverOne", "serverName1");
        createOnlineComServer("serverOne", "serverName2");
    }

    @Test
    @Transactional
    public void testGetComServerBySystemName() throws Exception {
        HostName.setCurrent("lapbvn");
        createOnlineComServer("serverOne");
        createOfflineComServer("lapbvn").makeObsolete();
        createOfflineComServer("lapbvn");
        ComServer comServerBySystemName = getEngineModelService().findComServerBySystemName().get();
        assertThat(comServerBySystemName.getName()).isEqualTo("lapbvn");
    }

    @Test
    @Transactional
    public void testDeletedComPortsAreNotVisible() throws Exception {
        HostName.setCurrent("lapbvn");
        OnlineComServer serverOne = createOnlineComServer("serverOne");
        OutboundComPort test = serverOne.newOutboundComPort("test", 1).comPortType(ComPortType.TCP).add();
        serverOne.removeComPort(test.getId());
        assertThat(getEngineModelService().findAllOutboundComPorts()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetAllOfflineComServers() throws Exception {
        HostName.setCurrent("lapbvn");
        createOfflineComServer("serverOne");
        createOfflineComServer("serverTwo").makeObsolete();
        createOfflineComServer("lapbvn");
        List<OfflineComServer> allOfflineComServers = getEngineModelService().findAllOfflineComServers();
        assertThat(allOfflineComServers).hasSize(2);
        assertThat(allOfflineComServers.get(0).getName()).isEqualTo("serverOne");
        assertThat(allOfflineComServers.get(1).getName()).isEqualTo("lapbvn");
    }

    @Test
    @Transactional
    public void testGetAllOnlineComServers() throws Exception {
        HostName.setCurrent("lapbvn");
        createOnlineComServer("serverOne");
        createOnlineComServer("serverTwo").makeObsolete();
        createOnlineComServer("lapbvn");
        List<OnlineComServer> allOnlineComServers = getEngineModelService().findAllOnlineComServers();
        assertThat(allOnlineComServers).hasSize(2);
        assertThat(allOnlineComServers.get(0).getName()).isEqualTo("serverOne");
        assertThat(allOnlineComServers.get(1).getName()).isEqualTo("lapbvn");
    }

    @Test
    @Transactional
    public void testGetAllRemoteComServers() throws Exception {
        HostName.setCurrent("lapbvn");
        OnlineComServer onlineComServer = createOnlineComServer("onlineServer");
        createRemoteComServer("serverOne", onlineComServer);
        createRemoteComServer("serverTwo", onlineComServer).makeObsolete();
        createRemoteComServer("lapbvn", onlineComServer);

        List<RemoteComServer> allRemoteComServers = getEngineModelService().findAllRemoteComServers();
        List<RemoteComServer> remoteComServersForOnlineComServer = getEngineModelService().findRemoteComServersForOnlineComServer(onlineComServer);

        assertThat(allRemoteComServers).hasSize(2);
        assertThat(allRemoteComServers.get(0).getName()).isEqualTo("serverOne");
        assertThat(allRemoteComServers.get(1).getName()).isEqualTo("lapbvn");

        assertThat(remoteComServersForOnlineComServer).hasSize(2);
        assertThat(allRemoteComServers.get(0).getName()).isEqualTo("serverOne");
        assertThat(allRemoteComServers.get(1).getName()).isEqualTo("lapbvn");
    }

    private OnlineComServer createOnlineComServer(String name) {
        return createOnlineComServer(name, name);
    }

    private OnlineComServer createOnlineComServer(String name, String serverName) {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServer = getEngineModelService().newOnlineComServerBuilder();
        onlineComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.communicationLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.changesInterPollDelay(new TimeDuration(600));
        onlineComServer.schedulingInterPollDelay(new TimeDuration(600));
        onlineComServer.storeTaskQueueSize(10);
        onlineComServer.storeTaskThreadPriority(3);
        onlineComServer.numberOfStoreTaskThreads(6);
        onlineComServer.name(name);
        onlineComServer.serverName(serverName);
        onlineComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServer.create();
    }

    private OfflineComServer createOfflineComServer(String name) {
        ComServer.ComServerBuilder<? extends OfflineComServer, ? extends ComServer.ComServerBuilder> offlineComServer = getEngineModelService().newOfflineComServerBuilder();
        offlineComServer.name(name);
        offlineComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        offlineComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        offlineComServer.changesInterPollDelay(new TimeDuration(600));
        offlineComServer.schedulingInterPollDelay(new TimeDuration(900));
        offlineComServer.active(false);
        return offlineComServer.create();
    }

    private RemoteComServer createRemoteComServer(String name, OnlineComServer onlineComServer) {
        RemoteComServer.RemoteComServerBuilder<? extends RemoteComServer> remoteComServer = getEngineModelService().newRemoteComServerBuilder();
        remoteComServer.name(name);
        remoteComServer.serverLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.communicationLogLevel(ComServer.LogLevel.DEBUG);
        remoteComServer.changesInterPollDelay(new TimeDuration(600));
        remoteComServer.schedulingInterPollDelay(new TimeDuration(900));
        remoteComServer.active(false);
        remoteComServer.onlineComServer(onlineComServer);
        remoteComServer.serverName(name);
        remoteComServer.statusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        remoteComServer.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return remoteComServer.create();
    }
}
