/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.CannotRemoveStateException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.NoPathLeftToSuccessFromStateException;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.UnreachableStateException;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.CREATED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.ONGOING;
import static com.elster.jupiter.servicecall.DefaultState.PARTIAL_SUCCESS;
import static com.elster.jupiter.servicecall.DefaultState.PAUSED;
import static com.elster.jupiter.servicecall.DefaultState.PENDING;
import static com.elster.jupiter.servicecall.DefaultState.REJECTED;
import static com.elster.jupiter.servicecall.DefaultState.SCHEDULED;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.elster.jupiter.servicecall.DefaultState.WAITING;
import static com.elster.jupiter.util.streams.Currying.test;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class ServiceCallLifeCycleBuilderIT {
    private static final String NAME = "theName";

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private final Instant now = ZonedDateTime.of(2016, 1, 8, 10, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    private Injector injector;

    private NlsService nlsService;
    private TransactionService transactionService;
    private MessageService messageService;
    private ServiceCallService serviceCallService;

    @Rule
    public TestRule expectedRule = new ExpectedExceptionRule();

    @Mock
    private UserService userService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private LogService logService;
    @Mock
    private MessageInterpolator messageInterpolator;

    private Clock clock;
    private CustomPropertySetService customPropertySetService;

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(MessageInterpolator.class).toInstance(messageInterpolator);
            bind(SearchService.class).toInstance(mock(SearchService.class));
//            bind(ServiceCallTypeOneCustomPropertySet.class).to(ServiceCallTypeOneCustomPropertySet.class);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() {
        clock = new ProgrammableClock(ZoneId.of("UTC"), now);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new DataVaultModule(),
                    new FiniteStateMachineModule(),
                    new CustomPropertySetsModule(),
                    new TimeModule(),
                    new BasicPropertiesModule(),
                    new ServiceCallModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(new Transaction<Void>() {

            @Override
            public Void perform() {
                nlsService = injector.getInstance(NlsService.class);
                customPropertySetService = injector.getInstance(CustomPropertySetService.class);
                messageService = injector.getInstance(MessageService.class);
                serviceCallService = injector.getInstance(ServiceCallService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void nameIsAsGiven() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            ServiceCallLifeCycle serviceCallLifeCycle = builder.create();

            assertThat(serviceCallLifeCycle.getName()).isEqualTo(NAME);
        }
    }

    @Test
    public void defaultContainsAllStatesAndTransitions() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();

            FiniteStateMachine finiteStateMachine = serviceCallLifeCycle.getFiniteStateMachine();

            Arrays.stream(DefaultState.values())
                    .forEach(state -> assertThat(contains(finiteStateMachine, state)).isTrue());

            Arrays.asList(
                    Pair.of(CREATED, REJECTED),
                    Pair.of(CREATED, PENDING),
                    Pair.of(CREATED, SCHEDULED),
                    Pair.of(SCHEDULED, PENDING),
                    Pair.of(SCHEDULED, CANCELLED),
                    Pair.of(PENDING, CANCELLED),
                    Pair.of(PENDING, ONGOING),
                    Pair.of(ONGOING, WAITING),
                    Pair.of(ONGOING, PAUSED),
                    Pair.of(ONGOING, CANCELLED),
                    Pair.of(ONGOING, SUCCESSFUL),
                    Pair.of(ONGOING, FAILED),
                    Pair.of(ONGOING, PARTIAL_SUCCESS),
                    Pair.of(WAITING, ONGOING),
                    Pair.of(WAITING, CANCELLED),
                    Pair.of(PAUSED, CANCELLED),
                    Pair.of(PAUSED, ONGOING),
                    Pair.of(PARTIAL_SUCCESS, SCHEDULED),
                    Pair.of(FAILED, SCHEDULED)
            ).stream()
                    .forEach(pair -> assertThat(containsTransition(finiteStateMachine, pair.getFirst(), pair.getLast()))
                            .describedAs("Does not contain transition from " + pair.getFirst() + " to " + pair.getLast())
                            .isTrue());
        }
    }

    @Test
    public void buildingWithoutTransitionFromPendingToCanceled() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            builder.removeTransition(PENDING, CANCELLED);

            ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();

            FiniteStateMachine finiteStateMachine = serviceCallLifeCycle.getFiniteStateMachine();

            assertThat(containsTransition(finiteStateMachine, PENDING, CANCELLED)).isFalse();
        }
    }

    @Test
    public void buildingWithoutCanceledState() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            builder.remove(CANCELLED);

            ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();

            FiniteStateMachine finiteStateMachine = serviceCallLifeCycle.getFiniteStateMachine();

            Arrays.stream(DefaultState.values())
                    .filter(not(CANCELLED::equals))
                    .forEach(state -> assertThat(contains(finiteStateMachine, state)).isTrue());

            Arrays.asList(
                    Pair.of(CREATED, REJECTED),
                    Pair.of(CREATED, PENDING),
                    Pair.of(CREATED, SCHEDULED),
                    Pair.of(SCHEDULED, PENDING),
                    Pair.of(PENDING, ONGOING),
                    Pair.of(ONGOING, WAITING),
                    Pair.of(ONGOING, PAUSED),
                    Pair.of(ONGOING, SUCCESSFUL),
                    Pair.of(ONGOING, FAILED),
                    Pair.of(ONGOING, PARTIAL_SUCCESS),
                    Pair.of(WAITING, ONGOING),
                    Pair.of(PAUSED, ONGOING),
                    Pair.of(PARTIAL_SUCCESS, SCHEDULED),
                    Pair.of(FAILED, SCHEDULED)
            ).stream()
                    .forEach(pair -> assertThat(containsTransition(finiteStateMachine, pair.getFirst(), pair.getLast()))
                            .describedAs("Does not contain transition from " + pair.getFirst() + " to " + pair.getLast())
                            .isTrue());
        }
    }

    @Test
    public void buildingWithoutCriticalStateFails() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            try {
                builder.remove(DefaultState.ONGOING);
                ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();
                fail("Expected CannotRemoveStateException");
            } catch (CannotRemoveStateException e) {
                // expected behaviour
            }

        }
    }

    @Test
    public void buildingWithBreakingPathsFails() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            try {
                builder.removeTransition(PENDING, ONGOING);
                ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();
                fail("Expected NoPathLeftToSuccessFromStateException");
            } catch (NoPathLeftToSuccessFromStateException e) {
                // expected behaviour
            }

        }
    }

    @Test
    public void buildingWithStuckState() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            try {
                builder.removeTransition(WAITING, ONGOING);
                ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();
                fail("Expected NoPathLeftToSuccessFromStateException");
            } catch (NoPathLeftToSuccessFromStateException e) {
                // expected behaviour
            }

        }
    }

    private boolean matches(DefaultState state, State fsmState) {
        return state.getKey().equals(fsmState.getName());
    }

    private boolean contains(FiniteStateMachine finiteStateMachine, DefaultState state) {
        return finiteStateMachine.getState(state.getKey()).isPresent();
    }

    private boolean containsTransition(FiniteStateMachine finiteStateMachine, DefaultState from, DefaultState to) {
        return finiteStateMachine.getState(from.getKey())
                .map(fromState -> fromState.getOutgoingStateTransitions()
                        .stream()
                        .map((StateTransition::getTo))
                        .anyMatch(test(this::matches).on(to)))
                .orElse(false);
    }

    @Test
    public void testCXO_609() {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder builder = serviceCallService.createServiceCallLifeCycle(NAME);

            try {
                builder.remove(SCHEDULED);
                builder.remove(PAUSED);
                builder.remove(WAITING);
                builder.remove(PARTIAL_SUCCESS);
                builder.remove(REJECTED);
                builder.removeTransition(ONGOING, CANCELLED);
                builder.removeTransition(PENDING, CANCELLED);
                builder.removeTransition(CREATED, CANCELLED);

                ServiceCallLifeCycleImpl serviceCallLifeCycle = (ServiceCallLifeCycleImpl) builder.create();
                fail("Expected NoPathLeftToSuccessFromStateException");
            } catch (UnreachableStateException e) {
                // expected behaviour
            }

        }

    }

}