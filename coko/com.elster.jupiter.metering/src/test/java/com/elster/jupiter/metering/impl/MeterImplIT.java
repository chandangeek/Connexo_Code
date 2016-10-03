package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration test for the {@link MeterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterImplIT {

    private Injector injector;

    @Rule
    public ExpectedExceptionRule exception = new ExpectedExceptionRule();
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
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
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
                    new BasicPropertiesModule(),
                    new TimeModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule(),
                    new CustomPropertySetsModule(),
                    new BasicPropertiesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
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

            // Business method
            Meter meter = meteringService.findAmrSystem(1).get()
                    .newMeter("amrID", "myName")
                    .setStateMachine(stateMachine)
                    .create();

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
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .create();
            meter.update();
            meter.activate(activation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation

        try (TransactionContext context = transactionService.getContext()) {
            meterActivation.endAt(deactivation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall

        try (TransactionContext context = transactionService.getContext()) {
            meter.activate(reinstall.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(reinstall.toInstant()));
    }

    @Test
    public void deactivateAndReinstallAdjacent() {
        ZonedDateTime activation = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime deactivation = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .create();
            meter.update();
            meter.activate(activation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation

        try (TransactionContext context = transactionService.getContext()) {
            meterActivation.endAt(deactivation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall

        try (TransactionContext context = transactionService.getContext()) {
            meter.activate(deactivation.toInstant());
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(deactivation.toInstant()));
    }

    @Test
    public void testCreateAndUpdateName() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .create();
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();

        String mRID = meter.getMRID();
        assertThat(mRID).isNotNull().isNotEmpty();
        assertThat(UUID.fromString(mRID).toString()).isEqualTo(mRID);
        assertThat(meter.getName()).isEqualTo("myName");

        try (TransactionContext context = transactionService.getContext()) {
            meter.setName("newName");
            meter.update();
            context.commit();
        }

        meter = meteringService.findMeterById(meter.getId()).get();

        assertThat(meter.getMRID()).isEqualTo(mRID);
        assertThat(meter.getName()).isEqualTo("newName");
    }

    @Test
    public void testCreateWithPrescribedMRID() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {
            meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .setMRID("00F000-0f7-0f00-f000-00ABCDE02ff")
                    .create();
            context.commit();
        }
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMRID()).isEqualTo("0000f000-00f7-0f00-f000-000abcde02ff");
    }

    @Test
    @Expected(IllegalArgumentException.class)
    public void testCreateWithWrongMRID() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);

        // activation

        try (TransactionContext context = transactionService.getContext()) {
            meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .setMRID("00000000-0000-00000000-0000000000ff")
                    .create();
            context.commit();
        }
    }

    private FiniteStateMachine createTinyFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = (FiniteStateMachineServiceImpl) this.injector.getInstance(FiniteStateMachineService.class);
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Tiny");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("TheOneAndOnly").complete());
        return stateMachine;
    }
}
