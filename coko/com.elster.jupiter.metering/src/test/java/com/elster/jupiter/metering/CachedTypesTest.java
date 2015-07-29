package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CachedTypesTest {

    private static Injector bootInjector;
    private static InMemoryBootstrapModule bootMemoryBootstrapModule = new InMemoryBootstrapModule();
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
        }
    }

    private static final boolean printSql = false;

    private static Injector getInjector(InMemoryBootstrapModule boot) {
        return Guice.createInjector(
                new MockModule(),
                boot,
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new UserModule(),
                new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(printSql),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule());
    }


    @BeforeClass
    public static void setUp() {
        bootInjector = getInjector(bootMemoryBootstrapModule);
        try (TransactionContext ctx = bootInjector.getInstance(TransactionService.class).getContext()) {
            bootInjector.getInstance(FiniteStateMachineService.class);
            bootInjector.getInstance(MeteringService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        bootMemoryBootstrapModule.deactivate();
    }

    @Before
    public void instanceSetup() throws SQLException {
        getInjector(inMemoryBootstrapModule);
    }

    @After
    public void instanceTearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }


    @Test
    public void testCachedTypes() {
        MeteringService meteringService = bootInjector.getInstance(MeteringService.class);
        assertThat(meteringService.getAvailableReadingTypes()).isNotEmpty();
        assertThat(meteringService.getServiceCategory(ServiceKind.HEAT)).isNotNull();
    }
}
