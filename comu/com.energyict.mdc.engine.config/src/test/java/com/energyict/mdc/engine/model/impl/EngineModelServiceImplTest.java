package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.Expected;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.HostName;
import com.energyict.mdc.engine.model.MockModule;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
    @Expected(expected = TranslatableApplicationException.class, messageId = "duplicateComServerX")
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
