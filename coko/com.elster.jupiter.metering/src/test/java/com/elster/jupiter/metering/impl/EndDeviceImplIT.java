package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceImplIT {
    private static Clock clock = mock(Clock.class);
    private static MeteringInMemoryBootstrapModule inMemoryPersistentModule =
            MeteringInMemoryBootstrapModule.withClockAndReadingTypes(clock, "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryPersistentModule.getTransactionService());

    @BeforeClass
    public static void beforeClass() {
        when(clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        inMemoryPersistentModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryPersistentModule.deactivate();
    }

    @Before
    public void setUp() {
        when(clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
    }

    @Test
    @Transactional
    public void testGetEvents() {
        Instant date = ZonedDateTime.of(2014, 2, 5, 14, 15, 16, 0, ZoneId.systemDefault()).toInstant();
        EndDevice endDevice;
        EndDeviceEventRecord eventRecord;
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();
        List<EndDeviceEventType> availableEndDeviceEventTypes = meteringService.getAvailableEndDeviceEventTypes();
        endDevice = meteringService.findAmrSystem(1).get().createEndDevice("1", "1");
        eventRecord = endDevice.addEventRecord(availableEndDeviceEventTypes.get(0), date).create();
        List<EndDeviceEventRecord> deviceEvents = endDevice.getDeviceEvents(Range.atLeast(date));
        assertThat(deviceEvents).contains(eventRecord);
        List<EndDeviceEventType> endDeviceEventTypes = new ArrayList<>(meteringService.getAvailableEndDeviceEventTypes());
        deviceEvents = endDevice.getDeviceEvents(Range.atLeast(date), endDeviceEventTypes);
        assertThat(deviceEvents).contains(eventRecord);
        endDeviceEventTypes.remove(0);
        deviceEvents = endDevice.getDeviceEvents(Range.atLeast(date), endDeviceEventTypes);
        assertThat(deviceEvents).isEmpty();
    }

    @Test
    @Transactional
    public void createEndDeviceWithManagedState() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "DeviceName");

        // Business method
        endDevice.update();

        // Asserts
        assertThat(endDevice.getFiniteStateMachine().isPresent()).isTrue();
        assertThat(endDevice.getFiniteStateMachine().get().getId()).isEqualTo(stateMachine.getId());
        assertThat(endDevice.getState().isPresent()).isTrue();
        assertThat(endDevice.getState().get().getId()).isEqualTo(stateMachine.getInitialState().getId());
    }

    @Test(expected = UnsupportedOperationException.class)
    @Transactional
    public void changeStateForDeviceWhoseStateIsNotManaged() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice("amrID", "DeviceName");

        // Business method
        endDevice.changeState(stateMachine.getInitialState(), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void changeStateToOneThatIsNotPartOfTheStateMachine() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
        FiniteStateMachine otherStateMachine = this.createBiggerFiniteStateMachine();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "DeviceName");

        // Business method
        endDevice.changeState(otherStateMachine.getInitialState(), Instant.now());

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void changeState() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();
        long deviceId;
        long stateId;
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "DeviceName");
        endDevice.update();
        deviceId = endDevice.getId();

        // Business method
        when(clock.instant()).thenReturn(now.plus(5, ChronoUnit.SECONDS));
        State second = stateMachine.getState("Second").get();
        stateId = second.getId();
        endDevice.changeState(second, Instant.now());

        when(clock.instant()).thenReturn(now.plus(10, ChronoUnit.SECONDS));
        EndDevice endDeviceReloaded = meteringService.findEndDeviceById(deviceId).get();

        // Asserts
        assertThat(endDeviceReloaded.getState().isPresent()).isTrue();
        assertThat(endDeviceReloaded.getState().get().getId()).isEqualTo(stateId);
    }

    @Test
    @Transactional
    public void changeStateInFuture() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);

        FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "name");
        // Business method

        when(clock.instant()).thenReturn(now.plus(1, ChronoUnit.HOURS));
        State second = stateMachine.getState("Second").get();
        endDevice.changeState(second, now.plus(2, ChronoUnit.HOURS));

        EndDevice endDeviceReloaded = meteringService.findEndDeviceById(endDevice.getId()).get();

        // Asserts
        assertThat(endDeviceReloaded.getState().isPresent()).isTrue();
        assertThat(endDeviceReloaded.getState().get().getId()).isEqualTo(stateMachine.getState("First").get().getId());
    }

    @Test
    @Transactional
    public void changingStateAndHistory() {
        Instant march1st = Instant.ofEpochMilli(1425168000000L);    // Midnight of March 1st, 2015 (GMT)
        Instant april1st = Instant.ofEpochMilli(1427846400000L);    // Midnight of April 1st, 2015 (GMT)

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();
        long deviceId;
        long initialStateId;
        long changedStateId;

        when(clock.instant()).thenReturn(march1st);
        FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
        State initialState = stateMachine.getInitialState();
        initialStateId = initialState.getId();
        State changedState = stateMachine.getState("Second").get();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "DeviceName");
        endDevice.update();
        deviceId = endDevice.getId();
        changedStateId = changedState.getId();
        when(clock.instant()).thenReturn(april1st);

        // Business method
        endDevice.changeState(changedState, april1st);

        EndDevice endDeviceReloaded = meteringService.findEndDeviceById(deviceId).get();

        // Asserts
        assertThat(endDeviceReloaded.getState().isPresent()).isTrue();
        assertThat(endDeviceReloaded.getState().get().getId()).isEqualTo(changedStateId);
        assertThat(endDeviceReloaded.getState(march1st).isPresent()).isTrue();
        assertThat(endDeviceReloaded.getState(march1st).get().getId()).isEqualTo(initialStateId);
        assertThat(endDeviceReloaded.getState(april1st).isPresent()).isTrue();
        assertThat(endDeviceReloaded.getState(april1st).get().getId()).isEqualTo(changedStateId);
    }

    @Test
    @Transactional
    public void getStateTimeline() {
        Instant march1st = Instant.ofEpochMilli(1425168000000L);    // Midnight of March 1st, 2015 (GMT)
        Instant april1st = Instant.ofEpochMilli(1427846400000L);    // Midnight of April 1st, 2015 (GMT)

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();
        long deviceId;
        long initialStateId;
        long changedStateId;

        when(clock.instant()).thenReturn(march1st);
        FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
        State initialState = stateMachine.getInitialState();
        initialStateId = initialState.getId();
        State changedState = stateMachine.getState("Second").get();
        ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().createEndDevice(stateMachine, "amrID", "DeviceName");
        endDevice.update();
        deviceId = endDevice.getId();
        changedStateId = changedState.getId();
        when(clock.instant()).thenReturn(april1st);
        endDevice.changeState(changedState, april1st);

        EndDevice endDeviceReloaded = meteringService.findEndDeviceById(deviceId).get();

        // Business method
        Optional<StateTimeline> stateTimeline = endDeviceReloaded.getStateTimeline();

        // Asserts
        assertThat(stateTimeline).isPresent();
        List<StateTimeSlice> slices = stateTimeline.get().getSlices();
        assertThat(slices).hasSize(2);
        assertThat(slices.get(0).getState().getId()).isEqualTo(initialStateId);
        assertThat(slices.get(0).getPeriod().lowerEndpoint()).isEqualTo(march1st);
        assertThat(slices.get(0).getPeriod().lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(slices.get(0).getPeriod().upperEndpoint()).isEqualTo(april1st);
        assertThat(slices.get(0).getPeriod().upperBoundType()).isEqualTo(BoundType.OPEN);
        assertThat(slices.get(1).getState().getId()).isEqualTo(changedStateId);
        assertThat(slices.get(1).getPeriod().lowerEndpoint()).isEqualTo(april1st);
        assertThat(slices.get(1).getPeriod().lowerBoundType()).isEqualTo(BoundType.CLOSED);
        assertThat(slices.get(1).getPeriod().hasUpperBound()).isFalse();
    }

    @Test
    @Transactional
    public void cimLifecycleDatesAreAllEmptyAtCreationTime() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "DeviceName");


        // Asserts
        LifecycleDates lifecycleDates = endDevice.getLifecycleDates();
        assertThat(lifecycleDates).isNotNull();
        assertThat(lifecycleDates.getManufacturedDate()).isEmpty();
        assertThat(lifecycleDates.getPurchasedDate()).isEmpty();
        assertThat(lifecycleDates.getReceivedDate()).isEmpty();
        assertThat(lifecycleDates.getInstalledDate()).isEmpty();
        assertThat(lifecycleDates.getRemovedDate()).isEmpty();
        assertThat(lifecycleDates.getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void cimLifecycleDatesAreAllEmptyAtCreationTimeAfterFind() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "DeviceName");

        // Business method
        LifecycleDates lifecycleDates = meteringService.findEndDeviceById(endDevice.getId()).get().getLifecycleDates();

        // Asserts
        assertThat(lifecycleDates).isNotNull();
        assertThat(lifecycleDates.getManufacturedDate()).isEmpty();
        assertThat(lifecycleDates.getPurchasedDate()).isEmpty();
        assertThat(lifecycleDates.getReceivedDate()).isEmpty();
        assertThat(lifecycleDates.getInstalledDate()).isEmpty();
        assertThat(lifecycleDates.getRemovedDate()).isEmpty();
        assertThat(lifecycleDates.getRetiredDate()).isEmpty();
    }

    @Test
    @Transactional
    public void updateCimLifecycleDates() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "DeviceName");

        // Business method
        LifecycleDates lifecycleDates = endDevice.getLifecycleDates();
        Instant manufacturedDate = Instant.ofEpochMilli(1000L);
        Instant purchasedDate = Instant.ofEpochMilli(2000L);
        Instant receivedDate = Instant.ofEpochMilli(3000L);
        Instant installedDate = Instant.ofEpochMilli(4000L);
        Instant removedDate = Instant.ofEpochMilli(5000L);
        Instant retiredDate = Instant.ofEpochMilli(6000L);
        lifecycleDates.setManufacturedDate(manufacturedDate);
        lifecycleDates.setPurchasedDate(purchasedDate);
        lifecycleDates.setReceivedDate(receivedDate);
        lifecycleDates.setInstalledDate(installedDate);
        lifecycleDates.setRemovedDate(removedDate);
        lifecycleDates.setRetiredDate(retiredDate);
        endDevice.update();

        // Asserts
        LifecycleDates datesAfterFind = meteringService.findEndDeviceById(endDevice.getId()).get().getLifecycleDates();
        assertThat(datesAfterFind).isNotNull();
        assertThat(datesAfterFind.getManufacturedDate()).contains(manufacturedDate);
        assertThat(datesAfterFind.getPurchasedDate()).contains(purchasedDate);
        assertThat(datesAfterFind.getReceivedDate()).contains(receivedDate);
        assertThat(datesAfterFind.getInstalledDate()).contains(installedDate);
        assertThat(datesAfterFind.getRemovedDate()).contains(removedDate);
        assertThat(datesAfterFind.getRetiredDate()).contains(retiredDate);
    }

    @Test
    @Transactional
    public void updateCimLifecycleDatesAfterFind() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "DeviceName");

        // Business method
        LifecycleDates lifecycleDates = endDevice.getLifecycleDates();
        Instant manufacturedDate = Instant.ofEpochMilli(1000L);
        Instant purchasedDate = Instant.ofEpochMilli(2000L);
        Instant receivedDate = Instant.ofEpochMilli(3000L);
        Instant installedDate = Instant.ofEpochMilli(4000L);
        Instant removedDate = Instant.ofEpochMilli(5000L);
        Instant retiredDate = Instant.ofEpochMilli(6000L);
        lifecycleDates.setManufacturedDate(manufacturedDate);
        lifecycleDates.setPurchasedDate(purchasedDate);
        lifecycleDates.setReceivedDate(receivedDate);
        lifecycleDates.setInstalledDate(installedDate);
        lifecycleDates.setRemovedDate(removedDate);
        lifecycleDates.setRetiredDate(retiredDate);
        endDevice.update();

        // Asserts
        assertThat(lifecycleDates).isNotNull();
        assertThat(lifecycleDates.getManufacturedDate()).contains(manufacturedDate);
        assertThat(lifecycleDates.getPurchasedDate()).contains(purchasedDate);
        assertThat(lifecycleDates.getReceivedDate()).contains(receivedDate);
        assertThat(lifecycleDates.getInstalledDate()).contains(installedDate);
        assertThat(lifecycleDates.getRemovedDate()).contains(removedDate);
        assertThat(lifecycleDates.getRetiredDate()).contains(retiredDate);
    }

    @Test
    @Transactional
    public void newDeviceIsNotObsolete() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "newDeviceIsNotObsolete");

        // Asserts
        assertThat(endDevice.isObsolete()).isFalse();
    }

    @Test
    @Transactional
    public void newDeviceIsAssignedAValidMRID() {
        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "newDeviceIsAssignedAValidMRID");

        String mRID = endDevice.getMRID();
        // Asserts
        assertThat(mRID).isNotNull().isNotEmpty();
        assertThat(UUID.fromString(mRID).toString()).isEqualTo(mRID);
    }

    @Test
    @Transactional
    public void makeObsolete() {
        Instant now = Instant.ofEpochSecond(10000);
        when(clock.instant()).thenReturn(now);

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        EndDevice endDevice = meteringService.findAmrSystem(1).get().createEndDevice("amrID", "makeObsolete");

        // Business method
        endDevice.makeObsolete();

        // Asserts
        assertThat(endDevice.isObsolete()).isTrue();
        assertThat(endDevice.getObsoleteTime()).contains(now);
    }

    @Test
    @Transactional
    public void findByIdAfterMakeObsolete() {
        Instant now = Instant.ofEpochSecond(10000);
        when(clock.instant()).thenReturn(now);

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("amrID", "findByIdAfterMakeObsolete")
                .create();
        meter.update();
        meter.makeObsolete();

        // Business method
        Optional<Meter> shouldBeObsolete = amrSystem.findMeter(meter.getAmrId());

        // Asserts
        assertThat(shouldBeObsolete).isPresent();
        assertThat(shouldBeObsolete.get().isObsolete()).isTrue();
        assertThat(meter.getObsoleteTime()).contains(now);
    }

    @Test
    @Transactional
    public void reuseSameName() {
        String name = "reuseSameName";

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        EndDevice willBeObsolete = amrSystem.createEndDevice("amrID1", name);
        willBeObsolete.makeObsolete();

        // Business method
        EndDevice endDevice = amrSystem.createEndDevice("amrID2", name);

        // Asserts
        assertThat(endDevice).isNotNull();
        assertThat(endDevice.getName()).isEqualTo(name);
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    @Transactional
    public void nameIsUnique() {
        String name = "nameIsUnique";

        MeteringService meteringService = inMemoryPersistentModule.getMeteringService();

        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        amrSystem.createEndDevice("amrID", name);
        amrSystem.createEndDevice("amrID", name);

        // Asserts: see expected exception rule
    }

    private FiniteStateMachine createTinyFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = (FiniteStateMachineServiceImpl) inMemoryPersistentModule.getFiniteStateMachineService();
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Tiny");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("TheOneAndOnly").complete());
        return stateMachine;
    }

    private FiniteStateMachine createBiggerFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = (FiniteStateMachineServiceImpl) inMemoryPersistentModule.getFiniteStateMachineService();
        CustomStateTransitionEventType eventType = finiteStateMachineService.newCustomStateTransitionEventType("#whatever", "enddevice");
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Bigger");
        State second = builder.newCustomState("Second").complete();
        State first = builder.newCustomState("First").on(eventType).transitionTo(second).complete();
        FiniteStateMachine stateMachine = builder.complete(first);
        return stateMachine;
    }
}
