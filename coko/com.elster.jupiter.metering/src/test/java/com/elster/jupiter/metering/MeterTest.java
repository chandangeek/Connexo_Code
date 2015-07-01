package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterTest {

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
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(false),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule()
        );
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
    public void testCOPL494() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter;
        Instant installDate1 = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant deactivateDate = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Instant installDate2 = ZonedDateTime.of(2015, 4, 12, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("myMeter");
            meter.save();
            ctx.commit();
        }
        assertThat(meter.getMeterActivations()).isEmpty();
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            meter.activate(installDate1);
            ctx.commit();
        }
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isPresent();
        assertThat(meter.getCurrentMeterActivation().get().getStart()).isEqualTo(installDate1);
        assertThat(meter.getCurrentMeterActivation().get().getEnd()).isNull();

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            meter.getCurrentMeterActivation().get().endAt(deactivateDate);
            ctx.commit();
        }
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isEmpty();
        assertThat(meter.getMeterActivations()).hasSize(1);
        assertThat(meter.getMeterActivations().get(0).getStart()).isEqualTo(installDate1);
        assertThat(meter.getMeterActivations().get(0).getEnd()).isEqualTo(deactivateDate);

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            meter.activate(installDate2);
            ctx.commit();
        }
        JupiterAssertions.assertThat(meter.getCurrentMeterActivation()).isPresent();
        assertThat(meter.getCurrentMeterActivation().get().getStart()).isEqualTo(installDate2);
        assertThat(meter.getCurrentMeterActivation().get().getEnd()).isNull();
        assertThat(meter.getMeterActivations()).hasSize(2);
        assertThat(meter.getMeterActivations().get(0).getStart()).isEqualTo(installDate1);
        assertThat(meter.getMeterActivations().get(0).getEnd()).isEqualTo(deactivateDate);
        assertThat(meter.getMeterActivations().get(1)).isEqualTo(meter.getCurrentMeterActivation().get());

    }

}
