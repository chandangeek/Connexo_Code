package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;

import java.sql.SQLException;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for the {@link FinateStateMachineServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-06 (15:32)
 */
public class FinateStateMachineServiceImplIT {

    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(FinateStateMachineServiceImplIT.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Test
    public void addStandardEventTypePredicateThatRejectsAllCandidates() {
        FinateStateMachineServiceImpl service = this.getTestService();
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
            FinateStateMachineServiceImpl service = this.getTestService();
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

    private void deleteAllStandardStateTransitionEventTypesIfAny() {
        try (TransactionContext context = getTransactionService().getContext()) {
            FinateStateMachineServiceImpl service = this.getTestService();
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

    private FinateStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFinateStateMachineService();
    }

}