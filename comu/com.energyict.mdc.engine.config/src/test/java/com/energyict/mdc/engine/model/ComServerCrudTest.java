package com.energyict.mdc.engine.model;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.engine.model.impl.EngineModelModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ComServerCrudTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() throws SQLException {
        injector = Guice.createInjector(
                inMemoryBootstrapModule,
                new EngineModelModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(true));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(EngineModelService.class);
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
    public void testCreateComServer() throws Exception {
        try (TransactionContext context = getTransactionService().getContext()) {
            OfflineComServer offlineComServer = getEngineModelService().newOfflineComServerInstance();
            offlineComServer.setName("Offliner");
            offlineComServer.setActive(false);
            offlineComServer.save();
            context.commit();
        }
    }
}
