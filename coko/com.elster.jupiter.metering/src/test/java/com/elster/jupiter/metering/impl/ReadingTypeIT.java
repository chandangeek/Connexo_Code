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
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
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
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


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
                    new MeteringModule(
                            "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // no macro period, no measuring period
                            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // no macro period, measuring period =  15 min
                            "11.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = day, no measuring period
                            "13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = month, no measuring period
                            "11.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", // macro period = day, measuring period =  15 min
                            "24.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0" // macro period = weekly, no measuring period
                    ),
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
                    new NlsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testFindEquidistantReadingTypes() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        List<ReadingType> equidistantTypes = meteringService.getAvailableEquidistantReadingTypes();
        List<ReadingType> nonEquidistantTypes = meteringService.getAvailableNonEquidistantReadingTypes();
        List<ReadingType> allTypes = meteringService.getAvailableReadingTypes();

        assertThat(equidistantTypes).isNotEmpty().isSubsetOf(allTypes);
        assertThat(nonEquidistantTypes).isNotEmpty().isSubsetOf(allTypes);

        List<ReadingType> intersection = new ArrayList<>(equidistantTypes);
        intersection.retainAll(nonEquidistantTypes);
        assertThat(intersection).isEmpty();

        List<ReadingType> union = new ArrayList<>(equidistantTypes);
        union.addAll(nonEquidistantTypes);
        assertThat(union).containsAll(allTypes);
    }


}