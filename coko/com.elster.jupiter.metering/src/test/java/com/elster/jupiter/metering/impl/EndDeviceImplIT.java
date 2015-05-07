package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineServiceImpl;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
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
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.BoundType;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndDeviceImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Clock clock;

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
        when(this.clock.instant()).thenAnswer(invocationOnMock -> Instant.now());
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
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
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
    public void testGetEvents() {
        Instant date = ZonedDateTime.of(2014, 2, 5, 14, 15, 16, 0, ZoneId.systemDefault()).toInstant();
        EndDevice endDevice;
        EndDeviceEventRecord eventRecord;
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            List<EndDeviceEventType> availableEndDeviceEventTypes = meteringService.getAvailableEndDeviceEventTypes();
            endDevice = meteringService.findAmrSystem(1).get().newEndDevice("1", "1");
            endDevice.save();
            eventRecord = endDevice.addEventRecord(availableEndDeviceEventTypes.get(0), date);
            eventRecord.save();
            context.commit();
        }
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
    public void createEndDeviceWithManagedState() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
            EndDevice endDevice = meteringService.findAmrSystem(1).get().newEndDevice(stateMachine, "amrID", "mRID");

            // Business method
            endDevice.save();

            // Asserts
            assertThat(endDevice.getFiniteStateMachine().isPresent()).isTrue();
            assertThat(endDevice.getFiniteStateMachine().get().getId()).isEqualTo(stateMachine.getId());
            assertThat(endDevice.getState().isPresent()).isTrue();
            assertThat(endDevice.getState().get().getId()).isEqualTo(stateMachine.getInitialState().getId());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void changeStateForDeviceWhoseStateIsNotManaged() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
            ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().newEndDevice("amrID", "mRID");

            // Business method
            endDevice.changeState(stateMachine.getInitialState());

            // Asserts: see expected exception rule
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void changeStateToOneThatIsNotPartOfTheStateMachine() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachine stateMachine = this.createTinyFiniteStateMachine();
            FiniteStateMachine otherStateMachine = this.createBiggerFiniteStateMachine();
            ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().newEndDevice(stateMachine, "amrID", "mRID");

            // Business method
            endDevice.changeState(otherStateMachine.getInitialState());

            // Asserts: see expected exception rule
        }
    }

    @Test
    public void changeState() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        long deviceId;
        long stateId;
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
            ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().newEndDevice(stateMachine, "amrID", "mRID");
            endDevice.save();
            deviceId = endDevice.getId();

            // Business method
            State second = stateMachine.getState("Second").get();
            stateId = second.getId();
            endDevice.changeState(second);
            context.commit();
        }

        EndDevice endDevice = meteringService.findEndDevice(deviceId).get();

        // Asserts
        assertThat(endDevice.getState().isPresent()).isTrue();
        assertThat(endDevice.getState().get().getId()).isEqualTo(stateId);
    }

    @Test
    public void changingStateAndHistory() {
        Instant march1st = Instant.ofEpochMilli(1425168000000L);    // Midnight of March 1st, 2015 (GMT)
        Instant april1st = Instant.ofEpochMilli(1427846400000L);    // Midnight of April 1st, 2015 (GMT)
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        long deviceId;
        long initialStateId;
        long changedStateId;
        try (TransactionContext context = transactionService.getContext()) {
            when(this.clock.instant()).thenReturn(march1st);
            FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
            State initialState = stateMachine.getInitialState();
            initialStateId = initialState.getId();
            State changedState = stateMachine.getState("Second").get();
            ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().newEndDevice(stateMachine, "amrID", "mRID");
            endDevice.save();
            deviceId = endDevice.getId();
            changedStateId = changedState.getId();
            when(this.clock.instant()).thenReturn(april1st);

            // Business method
            endDevice.changeState(changedState);
            context.commit();
        }

        EndDevice endDevice = meteringService.findEndDevice(deviceId).get();

        // Asserts
        assertThat(endDevice.getState().isPresent()).isTrue();
        assertThat(endDevice.getState().get().getId()).isEqualTo(changedStateId);
        assertThat(endDevice.getState(march1st).isPresent()).isTrue();
        assertThat(endDevice.getState(march1st).get().getId()).isEqualTo(initialStateId);
        assertThat(endDevice.getState(april1st).isPresent()).isTrue();
        assertThat(endDevice.getState(april1st).get().getId()).isEqualTo(changedStateId);
    }

    @Test
    public void getStateTimeline() {
        Instant march1st = Instant.ofEpochMilli(1425168000000L);    // Midnight of March 1st, 2015 (GMT)
        Instant april1st = Instant.ofEpochMilli(1427846400000L);    // Midnight of April 1st, 2015 (GMT)
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        long deviceId;
        long initialStateId;
        long changedStateId;
        try (TransactionContext context = transactionService.getContext()) {
            when(this.clock.instant()).thenReturn(march1st);
            FiniteStateMachine stateMachine = this.createBiggerFiniteStateMachine();
            State initialState = stateMachine.getInitialState();
            initialStateId = initialState.getId();
            State changedState = stateMachine.getState("Second").get();
            ServerEndDevice endDevice = (ServerEndDevice) meteringService.findAmrSystem(1).get().newEndDevice(stateMachine, "amrID", "mRID");
            endDevice.save();
            deviceId = endDevice.getId();
            changedStateId = changedState.getId();
            when(this.clock.instant()).thenReturn(april1st);
            endDevice.changeState(changedState);
            context.commit();
        }
        EndDevice endDevice = meteringService.findEndDevice(deviceId).get();

        // Business method
        Optional<StateTimeline> stateTimeline = endDevice.getStateTimeline();

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

    private FiniteStateMachine createTinyFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = this.injector.getInstance(FiniteStateMachineServiceImpl.class);
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Tiny");
        FiniteStateMachine stateMachine = builder.complete(builder.newCustomState("TheOneAndOnly").complete());
        stateMachine.save();
        return stateMachine;
    }

    private FiniteStateMachine createBiggerFiniteStateMachine() {
        FiniteStateMachineServiceImpl finiteStateMachineService = this.injector.getInstance(FiniteStateMachineServiceImpl.class);
        CustomStateTransitionEventType eventType = finiteStateMachineService.newCustomStateTransitionEventType("#whatever");
        eventType.save();
        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine("Bigger");
        State second = builder.newCustomState("Second").complete();
        State first = builder.newCustomState("First").on(eventType).transitionTo(second).complete();
        FiniteStateMachine stateMachine = builder.complete(first);
        stateMachine.save();
        return stateMachine;
    }

}
