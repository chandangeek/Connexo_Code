package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;

import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link StateTransitionEventTypeImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (09:39)
 */
public class StateTransitionEventTypeIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(StateTransitionEventTypeIT.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @Transactional
    @Test
    public void createWithoutViolations() {
        String symbol = "createWithoutViolations";
        StateTransitionEventType eventType = this.getTestService().newStateTransitionEventType(symbol);

        // Business method
        eventType.save();

        // Asserts
        assertThat(eventType.getSymbol()).isEqualTo(symbol);
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createWithNullSymbol() {
        StateTransitionEventType eventType = this.getTestService().newStateTransitionEventType(null);

        // Business method
        eventType.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void createWithEmptySymbol() {
        StateTransitionEventType eventType = this.getTestService().newStateTransitionEventType("");

        // Business method
        eventType.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @Test
    public void createWithTooLongSymbol() {
        StateTransitionEventType eventType = this.getTestService().newStateTransitionEventType(Strings.repeat("Symbol", 100));

        // Business method
        eventType.save();

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_EVENT_TYPE_SYMBOL)
    @Test
    public void createDuplicate() {
        String symbol = "First";
        StateTransitionEventType first = this.getTestService().newStateTransitionEventType(symbol);
        first.save();
        StateTransitionEventType withSameName = this.getTestService().newStateTransitionEventType(symbol);

        // Business method
        withSameName.save();

        // Asserts: see expected constraint violation rule
    }

    private FinateStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFinateStateMachineService();
    }

}