package com.energyict.mdc.engine.model.impl;

import java.util.List;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.protocol.api.ComPortType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EngineModelServiceImplTest extends PersistenceTest {

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
        createOnlineComServer("serverOne");
        createOnlineComServer("serverOne");
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
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(600));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(600));
        onlineComServer.setStoreTaskQueueSize(10);
        onlineComServer.setStoreTaskThreadPriority(3);
        onlineComServer.setNumberOfStoreTaskThreads(6);
        onlineComServer.setName(name);
        onlineComServer.save();
        return onlineComServer;
    }

    private OfflineComServer createOfflineComServer(String name) {
        OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
        offlineComServer.setName(name);
        offlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        offlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        offlineComServer.setChangesInterPollDelay(new TimeDuration(600));
        offlineComServer.setSchedulingInterPollDelay(new TimeDuration(900));
        offlineComServer.setActive(false);
        offlineComServer.save();
        return offlineComServer;
    }
    
    private RemoteComServer createRemoteComServer(String name, OnlineComServer onlineComServer) {
        RemoteComServer remoteComServer = getEngineModelService().newRemoteComServerInstance();
        remoteComServer.setName(name);
        remoteComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        remoteComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
        remoteComServer.setChangesInterPollDelay(new TimeDuration(600));
        remoteComServer.setSchedulingInterPollDelay(new TimeDuration(900));
        remoteComServer.setActive(false);
        remoteComServer.setOnlineComServer(onlineComServer);
        remoteComServer.save();
        return remoteComServer;
    }
}
