package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link MeterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterImplIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void createEndDeviceWithManagedState() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
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

    @Test
    @Transactional
    public void deviceCreatedInPastHasInitialStateInPast() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
        Instant twoHoursAgo = inMemoryBootstrapModule.getClock().instant().minus(2, ChronoUnit.HOURS);

        // Business method
        Meter meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get()
                .newMeter("amrID", "name")
                .setReceivedDate(twoHoursAgo)
                .setStateMachine(stateMachine)
                .create();
        meter = meteringService.findMeterById(meter.getId()).get();

        // Asserts
        assertThat(meter.getFiniteStateMachine().isPresent()).isTrue();
        assertThat(meter.getFiniteStateMachine().get().getId()).isEqualTo(stateMachine.getId());
        assertThat(meter.getState(twoHoursAgo).isPresent()).isTrue();
        assertThat(meter.getState(twoHoursAgo).get().getId()).isEqualTo(stateMachine.getInitialState().getId());
    }

    @Test
    @Transactional
    public void deviceCreatedInFutureHasInitialStateNow() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
        Instant now = inMemoryBootstrapModule.getClock().instant().plus(10, ChronoUnit.MINUTES);
        Instant twoHoursAfter = now.plus(2, ChronoUnit.HOURS);

        // Business method
        Meter meter = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get()
                .newMeter("amrID", "name")
                .setReceivedDate(twoHoursAfter)
                .setStateMachine(stateMachine)
                .create();
        meter = meteringService.findMeterById(meter.getId()).get();

        // Asserts
        assertThat(meter.getFiniteStateMachine().isPresent()).isTrue();
        assertThat(meter.getFiniteStateMachine().get().getId()).isEqualTo(stateMachine.getId());
        assertThat(meter.getState(now).isPresent()).isTrue();
        assertThat(meter.getState(now).get().getId()).isEqualTo(stateMachine.getInitialState().getId());
    }

    @Test
    @Transactional
    public void deactivateAndReinstallNonAdjacent() {
        ZonedDateTime activation = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime deactivation = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime reinstall = ZonedDateTime.of(2015, 4, 12, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        // activation
        Meter meter = meteringService.findAmrSystem(1).get()
                .newMeter("amrID", "myName")
                .create();
        meter.update();
        meter.activate(activation.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation
        meterActivation.endAt(deactivation.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall
        meter.activate(reinstall.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(reinstall.toInstant()));
    }

    @Test
    @Transactional
    public void deactivateAndReinstallAdjacent() {
        ZonedDateTime activation = ZonedDateTime.of(2015, 4, 10, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime deactivation = ZonedDateTime.of(2015, 4, 11, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        ZonedDateTime reinstall = deactivation;
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        // activation
        Meter meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                .create();
        meter.update();
        meter.activate(activation.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        MeterActivation meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(activation.toInstant()));

        // deactivation
        meterActivation.endAt(deactivation.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(1);
        meterActivation = meter.getMeterActivations().get(0);
        assertThat(meterActivation.getRange()).isEqualTo(Range.closedOpen(activation.toInstant(), deactivation.toInstant()));

        // reinstall
        meter.activate(reinstall.toInstant());
        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMeterActivations()).hasSize(2);
        meterActivation = meter.getMeterActivations().get(1);
        assertThat(meterActivation.getRange()).isEqualTo(Range.atLeast(reinstall.toInstant()));
    }

    @Test
    @Transactional
    public void testCreateAndUpdateName() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        // activation

        Meter meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                .create();
        meter = meteringService.findMeterById(meter.getId()).get();

        String mRID = meter.getMRID();
        assertThat(mRID).isNotNull().isNotEmpty();
        assertThat(UUID.fromString(mRID).toString()).isEqualTo(mRID);
        assertThat(meter.getName()).isEqualTo("myName");

        meter.setName("newName");
        meter.update();

        meter = meteringService.findMeterById(meter.getId()).get();

        assertThat(meter.getMRID()).isEqualTo(mRID);
        assertThat(meter.getName()).isEqualTo("newName");
    }

    @Test
    @Transactional
    public void testCreateWithPrescribedMRID() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        // activation
        Meter meter = meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                    .setMRID("00F000-0f7-0f00-f000-00ABCDE02ff")
                    .create();

        meter = meteringService.findMeterById(meter.getId()).get();
        assertThat(meter.getMRID()).isEqualTo("0000f000-00f7-0f00-f000-000abcde02ff");
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void testCreateWithWrongMRID() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();

        // activation
        meteringService.findAmrSystem(1).get().newMeter("amrID", "myName")
                .setMRID("00000000-0000-00000000-0000000000ff")
                .create();
    }

    private FiniteStateMachine createTinyFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = (FiniteStateMachineServiceImpl) inMemoryBootstrapModule.getFiniteStateMachineService();
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Tiny");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("TheOneAndOnly").complete());
        return stateMachine;
    }
}
