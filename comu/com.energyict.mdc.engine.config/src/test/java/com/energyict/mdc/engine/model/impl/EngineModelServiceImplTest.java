package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
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
    @ExpectedConstraintViolation(messageId = "{MDC.DuplicateComServer}")
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
        ComServer comServerBySystemName = getEngineModelService().findComServerBySystemName();
        assertThat(comServerBySystemName.getName()).isEqualTo("lapbvn");
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
}
