package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.MockModule;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EngineModelServiceImplTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new MdcCommonModule(),
                new TransactionModule(true),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(EngineModelService.class);
        	injector.getInstance(EnvironmentImpl.class); // fake call to make sure component is initialized
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private EngineModelService getEngineModelService() {
        return injector.getInstance(EngineModelService.class);
    }

    @Test
    public void testCanCreateComServerWithSameNameAsObsoleteComServer() throws Exception {
        createOnlineComServer("serverOne", true);
        createOnlineComServer("serverOne", false);
    }

    @Test
    public void testCanNotCreateComServerWithSameNameAsNonObsoleteComServer() throws Exception {
        createOnlineComServer("serverOne", false);
        createOnlineComServer("serverOne", false);
    }

    @Test
    public void testGetComServerBySystemName() throws Exception {
        HostName.setCurrent("lapbvn");
        createOnlineComServer("serverOne", true);
        createOfflineComServer("lapbvn", true);
        createOfflineComServer("lapbvn", false);
        ComServer comServerBySystemName = getEngineModelService().findComServerBySystemName();
        assertThat(comServerBySystemName.getName()).isEqualTo("lapbvn");
    }

    private OnlineComServer createOnlineComServer(String name, boolean obsolete) {
        try (TransactionContext context= getTransactionService().getContext()) {
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
            if (obsolete) {
                onlineComServer.makeObsolete();
            }
            context.commit();
            return onlineComServer;
        }
    }

    private OfflineComServer createOfflineComServer(String name, boolean obsolete) {
        try (TransactionContext context= getTransactionService().getContext()) {
            OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
            offlineComServer.setName(name);
            offlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
            offlineComServer.setCommunicationLogLevel(ComServer.LogLevel.DEBUG);
            offlineComServer.setChangesInterPollDelay(new TimeDuration(600));
            offlineComServer.setSchedulingInterPollDelay(new TimeDuration(900));
            offlineComServer.setActive(false);
            offlineComServer.save();
            if (obsolete) {
                offlineComServer.makeObsolete();
            }
            context.commit();
            return offlineComServer;
        }

    }
}
