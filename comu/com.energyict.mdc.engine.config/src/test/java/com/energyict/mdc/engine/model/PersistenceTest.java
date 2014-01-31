package com.energyict.mdc.engine.model;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PersistenceTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedErrorRule();

    @BeforeClass
    public static void staticSetUp() throws SQLException {
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
    public static void staticTearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    @After
    public void tearDown() throws Exception {
        clearDB();

    }

    private void clearDB() {
        for (ComServer comServer : getEngineModelService().findAllRemoteComServers()) {
            for (ComPort comPort : comServer.getComPorts()) {
                comServer.removeComPort(comPort.getId());
            }
            comServer.delete();
        }
        for (ComServer comServer : getEngineModelService().findAllComServers().find()) {
            for (ComPort comPort : comServer.getComPorts()) {
                comServer.removeComPort(comPort.getId());
            }
            comServer.delete();
        }
        for (ComPortPool comPortPool : getEngineModelService().findAllComPortPools()) {
            comPortPool.delete();
        }
    }


    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public final EngineModelService getEngineModelService() {
        return injector.getInstance(EngineModelService.class);
    }

}
