package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link FiniteStateMachineServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-06 (15:32)
 */
public class FiniteStateMachineServiceImplIT {

    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(FiniteStateMachineServiceImplIT.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Test
    public void stateTransitionChangeEventTopicIsNotEmpty() {
        // Business method
        String topic = this.getTestService().stateTransitionChangeEventTopic();

        // Asserts
        assertThat(topic).isNotEmpty();
    }

    @Test
    public void addStandardEventTypePredicateThatRejectsAllCandidates() {
        FiniteStateMachineServiceImpl service = this.getTestService();
        StandardEventPredicate predicate = mock(StandardEventPredicate.class);
        when(predicate.isCandidate(any(EventType.class))).thenReturn(false);

        // Business method
        service.addStandardEventPredicate(predicate);

        // Asserts
        assertThat(
            inMemoryPersistence.getService(EventService.class)
                    .getEventTypes()
                    .stream()
                    .noneMatch(EventType::isEnabledForUseInStateMachines))
            .isTrue();
    }

    @Test
    public void addStandardEventTypePredicateThatAcceptsAllCandidates() {
        try {
            FiniteStateMachineServiceImpl service = this.getTestService();
            StandardEventPredicate predicate = mock(StandardEventPredicate.class);
            when(predicate.isCandidate(any(EventType.class))).thenReturn(true);

            // Business method
            service.addStandardEventPredicate(predicate);

            // Asserts
            assertThat(
                inMemoryPersistence.getService(EventService.class)
                        .getEventTypes()
                        .stream()
                        .allMatch(EventType::isEnabledForUseInStateMachines))
                .isTrue();
        }
        finally {
            this.deleteAllStandardStateTransitionEventTypesIfAny();
        }
    }

    @Test
    public void getStateTransitionEventTypes() {
        try {
            FiniteStateMachineServiceImpl service = this.getTestService();
            StandardEventPredicate predicate = mock(StandardEventPredicate.class);
            when(predicate.isCandidate(any(EventType.class))).thenReturn(true);

            // Business method
            service.addStandardEventPredicate(predicate);

            // Asserts
            assertThat(service.getStateTransitionEventTypes()).hasSize(
                    inMemoryPersistence.getService(EventService.class).getEventTypes().size());
        }
        finally {
            this.deleteAllStandardStateTransitionEventTypesIfAny();
        }
    }

    @Test
    public void findBySymbolThatDoesNotExist() {
        TransactionService transactionService = inMemoryPersistence.getService(TransactionService.class);
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachineServiceImpl service = this.getTestService();

            // Business method
            Optional<StateTransitionEventType> eventType = service.findStateTransitionEventTypeBySymbol("#findBySymbolThatDoesNotExist");
            context.commit();

            // Asserts
            assertThat(eventType.isPresent()).isFalse();

        }
        finally {
            this.deleteAllStandardStateTransitionEventTypesIfAny();
        }
    }

    @Test
    public void findBySymbolForCustomEventType() {
        TransactionService transactionService = inMemoryPersistence.getService(TransactionService.class);
        ThreadPrincipalService threadPrincipalService = inMemoryPersistence.getService(ThreadPrincipalService.class);
        threadPrincipalService.set(() -> "test");
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachineServiceImpl service = this.getTestService();
            String symbol = "#findBySymbolForCustomEventType";
            CustomStateTransitionEventType custom = service.newCustomStateTransitionEventType(symbol);
            custom.save();

            // Business method
            Optional<StateTransitionEventType> eventType = service.findStateTransitionEventTypeBySymbol(symbol);
            context.commit();

            // Asserts
            assertThat(eventType.isPresent()).isTrue();
            assertThat(eventType.get().getSymbol()).isEqualTo(symbol);
        }
        finally {
            this.deleteAllStandardStateTransitionEventTypesIfAny();
            threadPrincipalService.clear();
        }
    }

    @Test
    public void findBySymbolForStandardEventType() {
        TransactionService transactionService = inMemoryPersistence.getService(TransactionService.class);
        ThreadPrincipalService threadPrincipalService = inMemoryPersistence.getService(ThreadPrincipalService.class);
        threadPrincipalService.set(() -> "Test");
        try (TransactionContext context = transactionService.getContext()) {
            FiniteStateMachineServiceImpl service = this.getTestService();
            String symbol = com.elster.jupiter.fsm.impl.EventType.TRIGGER_EVENT.topic();
            EventService eventService = inMemoryPersistence.getService(EventService.class);
            EventType standardEventType = eventService.getEventType(symbol).get();
            StandardStateTransitionEventType standard = service.newStandardStateTransitionEventType(standardEventType);
            standard.save();

            // Business method
            Optional<StateTransitionEventType> eventType = service.findStateTransitionEventTypeBySymbol(symbol);
            context.commit();

            // Asserts
            assertThat(eventType.isPresent()).isTrue();
            assertThat(eventType.get().getSymbol()).isEqualTo(symbol);
        }
        finally {
            this.deleteAllStandardStateTransitionEventTypesIfAny();
            threadPrincipalService.clear();
        }
    }

    private void deleteAllStandardStateTransitionEventTypesIfAny() {
        try (TransactionContext context = getTransactionService().getContext()) {
            FiniteStateMachineServiceImpl service = this.getTestService();
            inMemoryPersistence.getService(EventService.class)
                    .getEventTypes()
                    .stream()
                    .filter(EventType::isEnabledForUseInStateMachines)
                    .map(service::findStandardStateTransitionEventType)
                    .flatMap(Functions.asStream())
                    .forEach(StandardStateTransitionEventType::delete);
            context.commit();
        }
    }

    private FiniteStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFiniteStateMachineService();
    }

}