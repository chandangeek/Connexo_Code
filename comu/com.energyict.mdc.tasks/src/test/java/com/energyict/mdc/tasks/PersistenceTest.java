package com.energyict.mdc.tasks;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.common.impl.MdcCommonModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.SQLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.osgi.framework.BundleContext;

import static org.mockito.Mockito.mock;

public class PersistenceTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void staticSetUp() throws SQLException {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new OrmModule(),
                new UtilModule(),
                new NlsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new MdcCommonModule(),
                new InMemoryMessagingModule(),
                new EventsModule(),
                new PluggableModule(),
                new TransactionModule(true),
                new TasksModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
        	injector.getInstance(EnvironmentImpl.class); // fake call to make sure component is initialized
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
//            injector.getInstance(TaskService.class); // fake call to make sure component is initialized
            ctx.commit();
        }
    }

    @AfterClass
    public static void staticTearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public final TaskService getTaskService() {
        return injector.getInstance(TaskService.class);
    }

}
