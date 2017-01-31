/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import com.google.common.base.Strings;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link StateTransitionEventTypeImpl} class hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-03 (09:39)
 */
public class StateTransitionEventTypeIT {

    public static final String EVENT1_TOPIC = "com/elster/jupiter/fsm/test/EVT1";
    public static final String EVENT2_TOPIC = "com/elster/jupiter/fsm/test/EVT2";
    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(StateTransitionEventTypeIT.class.getSimpleName());
        createEventTypes();
    }

    private static void createEventTypes() {
        try (TransactionContext context = inMemoryPersistence.getTransactionService().getContext()) {
            createEventType(EVENT1_TOPIC, "EVENT1");
            createEventType(EVENT2_TOPIC, "EVENT2");
            context.commit();
        }
    }

    private static void createEventType(String topic, String name) {
        EventService eventService = inMemoryPersistence.getService(EventService.class);
        eventService.buildEventTypeWithTopic(topic)
                .shouldNotPublish()
                .component(FiniteStateMachineService.COMPONENT_NAME)
                .name(name)
                .scope("TEST")
                .category("TEST")
                .create();
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
    public void createCustomWithoutViolations() {
        String symbol = "createCustomWithoutViolations";

        // Business method
        CustomStateTransitionEventType eventType = this.getTestService()
                .newCustomStateTransitionEventType(symbol, "COMP");

        // Asserts
        assertThat(eventType.getSymbol()).isEqualTo(symbol);
    }

    @Transactional
    @Test
    public void createMultipleCustomWithoutViolations() {
        // Business method
        CustomStateTransitionEventType eventType1 = this.getTestService()
                .newCustomStateTransitionEventType("symbol1", "COMP");
        CustomStateTransitionEventType eventType2 = this.getTestService()
                .newCustomStateTransitionEventType("symbol2", "COMP");

        // Asserts
        assertThat(eventType1).isNotNull();
        assertThat(eventType2).isNotNull();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "symbol")
    @Test
    public void createCustomWithNullSymbol() {
        // Business method
        CustomStateTransitionEventType eventType = this.getTestService()
                .newCustomStateTransitionEventType(null, "COMP");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "symbol")
    @Test
    public void createCustomWithEmptySymbol() {
        // Business method
        CustomStateTransitionEventType eventType = this.getTestService().newCustomStateTransitionEventType("", "COMP");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "context")
    @Test
    public void createCustomWithNullContext() {
        // Business method
        CustomStateTransitionEventType eventType = this.getTestService()
                .newCustomStateTransitionEventType("symbol", null);

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "context")
    @Test
    public void createCustomWithEmptyContext() {
        // Business method
        CustomStateTransitionEventType eventType = this.getTestService()
                .newCustomStateTransitionEventType("SYmbol", "");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "symbol")
    @Test
    public void createCustomWithTooLongSymbol() {
        // Business method
        CustomStateTransitionEventType eventType = this.getTestService()
                .newCustomStateTransitionEventType(Strings.repeat("Symbol", 100), "COMP");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = MessageSeeds.Keys.UNIQUE_EVENT_TYPE_SYMBOL)
    @Test
    public void createCustomDuplicate() {
        String symbol = "First";
        this.getTestService().newCustomStateTransitionEventType(symbol, "COMP");

        // Business method
        this.getTestService().newCustomStateTransitionEventType(symbol, "COMP");

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void createStandardWithoutViolations() {
        com.elster.jupiter.events.EventType eventType = inMemoryPersistence.getService(EventService.class).getEventType(EVENT1_TOPIC).get();

        // Business method
        StandardStateTransitionEventType stateTransitionEventType = this.getTestService().newStandardStateTransitionEventType(eventType);

        // Asserts
        assertThat(stateTransitionEventType.getEventType().getTopic()).isEqualTo(EVENT1_TOPIC);
        assertThat(stateTransitionEventType.getSymbol()).isEqualTo(EVENT1_TOPIC);
        assertThat(eventType.isEnabledForUseInStateMachines()).isTrue();
    }

    @Transactional
    @Test
    public void createMultipleStandardWithoutViolations() {
        com.elster.jupiter.events.EventType eventType1 = inMemoryPersistence.getService(EventService.class).getEventType(EVENT1_TOPIC).get();
        com.elster.jupiter.events.EventType eventType2 = inMemoryPersistence.getService(EventService.class).getEventType(EVENT2_TOPIC).get();

        // Business method
        StandardStateTransitionEventType stateTransitionEventType1 = this.getTestService().newStandardStateTransitionEventType(eventType1);
        StandardStateTransitionEventType stateTransitionEventType2 = this.getTestService().newStandardStateTransitionEventType(eventType2);

        // Asserts
        assertThat(stateTransitionEventType1).isNotNull();
        assertThat(stateTransitionEventType2).isNotNull();
        assertThat(eventType1.isEnabledForUseInStateMachines()).isTrue();
        assertThat(eventType2.isEnabledForUseInStateMachines()).isTrue();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "eventType", strict = false)
    @Test
    public void createStandardWithoutEventType() {
        // Business method
        StandardStateTransitionEventType stateTransitionEventType = this.getTestService().newStandardStateTransitionEventType(null);

        // Asserts
        assertThat(stateTransitionEventType.getEventType().getTopic()).isEqualTo(EVENT1_TOPIC);
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNIQUE_STANDARD_EVENT_TYPE + "}")
    @Test
    public void createStandardDuplicates() {
        com.elster.jupiter.events.EventType eventType = inMemoryPersistence.getService(EventService.class).getEventType(EVENT1_TOPIC).get();
        this.getTestService().newStandardStateTransitionEventType(eventType);

        // Business method
        this.getTestService().newStandardStateTransitionEventType(eventType);

        // Asserts: see expected constraint violation rule
    }

    @Transactional
    @Test
    public void deleteStandardClearsEnabledFlagOnJupiterEventType() {
        com.elster.jupiter.events.EventType eventType = inMemoryPersistence.getService(EventService.class).getEventType(EVENT1_TOPIC).get();
        StandardStateTransitionEventType stateTransitionEventType = this.getTestService().newStandardStateTransitionEventType(eventType);

        // Business method
        stateTransitionEventType.delete();

        // Asserts
        assertThat(stateTransitionEventType.getEventType().getTopic()).isEqualTo(EVENT1_TOPIC);
        assertThat(stateTransitionEventType.getSymbol()).isEqualTo(EVENT1_TOPIC);
        assertThat(eventType.isEnabledForUseInStateMachines()).isFalse();
    }

    private FiniteStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFiniteStateMachineService();
    }

}