package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
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
import com.google.common.collect.Range;
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
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link MeterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterImplIT {

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
                    new MeteringModule(false),
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
    public void createEndDeviceWithManagedState() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
            Meter meter = meteringService.findAmrSystem(1).get().newMeter(stateMachine, "amrID", "mRID");

            // Business method
            meter.save();

            // Asserts
            assertThat(meter.getFiniteStateMachine().isPresent()).isTrue();
            assertThat(meter.getFiniteStateMachine().get().getId()).isEqualTo(stateMachine.getId());
            assertThat(meter.getState().isPresent()).isTrue();
            assertThat(meter.getState().get().getId()).isEqualTo(stateMachine.getInitialState().getId());
        }
    }

    @Test
    public void deactivateAndReinstallNonAdjacent() {
        ZonedDateTime activation = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime deactivation = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime reinstall = ZonedDateTime.of(2015, 4, 12, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "mRID");
            meter.save();
            meter.activate(activation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation

        try (TransactionContext context = transactionService.getContext()) {
            meterActivation.endAt(deactivation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall

        try (TransactionContext context = transactionService.getContext()) {
            meter.activate(reinstall.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(reinstall.toInstant()));

    }

    @Test
    public void deactivateAndReinstallAdjacent() {
        ZonedDateTime activation = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime deactivation = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime reinstall = deactivation;
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "mRID");
            meter.save();
            meter.activate(activation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation

        try (TransactionContext context = transactionService.getContext()) {
            meterActivation.endAt(deactivation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall

        try (TransactionContext context = transactionService.getContext()) {
            meter.activate(reinstall.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(reinstall.toInstant()));

    }

    private FiniteStateMachine createTinyFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = this.injector.getInstance(FiniteStateMachineServiceImpl.class);
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Tiny");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("TheOneAndOnly").complete());
        stateMachine.save();
        return stateMachine;
    }

}