package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link DeviceLifeCycleImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (12:01)
 */
public class DeviceLifeCycleIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceLifeCycleIT.class.getSimpleName());
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
    public void createNewEmptyDeviceLifeCycle() {
        FinateStateMachine stateMachine = this.findDefaultFinateStateMachine();

        // Business method
        DeviceLifeCycle deviceLifeCycle = this.getTestService().newDeviceLifeCycleUsing(stateMachine);
        deviceLifeCycle.save();

        // Asserts
        assertThat(deviceLifeCycle.getFinateStateMachine()).isNotNull();
        assertThat(deviceLifeCycle.getFinateStateMachine().getId()).isEqualTo(stateMachine.getId());
        assertThat(deviceLifeCycle.getId()).isGreaterThan(0);
        assertThat(deviceLifeCycle.getName()).isNotNull();
        assertThat(deviceLifeCycle.getCreationTimestamp()).isNotNull();
        assertThat(deviceLifeCycle.getVersion()).isNotNull();
    }

    private FinateStateMachine findDefaultFinateStateMachine() {
        return inMemoryPersistence
                .getService(FinateStateMachineService.class)
                .findFinateStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_FINATE_STATE_MACHINE_NAME.getDefaultFormat())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finate state machine"));
    }

    private DeviceLifeCycleService getTestService() {
        return inMemoryPersistence.getDeviceLifeCycleService();
    }

}