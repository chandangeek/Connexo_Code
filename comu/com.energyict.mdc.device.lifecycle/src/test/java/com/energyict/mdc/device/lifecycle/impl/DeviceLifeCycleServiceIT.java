package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link DeviceLifeCycleServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (13:12)
 */
public class DeviceLifeCycleServiceIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceLifeCycleServiceIT.class.getSimpleName());
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
    public void installerCreatedTheDefaultFinateStateMachine() {
        FinateStateMachineService finateStateMachineService = inMemoryPersistence.getService(FinateStateMachineService.class);

        // Business method: actually the business method is the install method of the DeviceLifeCycleServiceImpl component
        Optional<FinateStateMachine> stateMachine = finateStateMachineService
                    .findFinateStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_FINATE_STATE_MACHINE_NAME.getDefaultFormat());

        // Asserts
        assertThat(stateMachine.isPresent()).isTrue();
    }

}