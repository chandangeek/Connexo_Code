package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HistoryIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Subscriber topicHandler;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private MeteringServiceImpl meteringService;
    private TransactionService transactionService;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            meteringService = (MeteringServiceImpl) injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testHistory() throws InterruptedException {
        ServiceCategoryImpl serviceCategory = (ServiceCategoryImpl) meteringService.getServiceCategory(ServiceKind.GAS).get();
        Instant first = Instant.now();
        Thread.sleep(5000);
        try (TransactionContext ctx = transactionService.getContext()) {
            serviceCategory.setAliasName("GGG");
            serviceCategory.update();
            ctx.commit();
        }
        Instant second = Instant.now();
        Thread.sleep(5000);
        try (TransactionContext ctx = transactionService.getContext()) {
            serviceCategory.setAliasName("HHH");
            serviceCategory.update();
            ctx.commit();
        }

        History<? extends ServiceCategory> history = serviceCategory.getHistory();

        Optional<? extends ServiceCategory> versionAt = history.getVersionAt(first);
        assertThat(versionAt).isPresent();
        ServiceCategory version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(1);
        assertThat(version.getAliasName()).isNull();

        versionAt = history.getVersionAt(second);
        assertThat(versionAt).isPresent();
        version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(2);
        assertThat(version.getAliasName()).isEqualTo("GGG");

        versionAt = history.getVersionAt(Instant.now());
        assertThat(versionAt).isPresent();
        version = versionAt.get();
        assertThat(version.getVersion()).isEqualTo(3);
        assertThat(version.getAliasName()).isEqualTo("HHH");
    }


}