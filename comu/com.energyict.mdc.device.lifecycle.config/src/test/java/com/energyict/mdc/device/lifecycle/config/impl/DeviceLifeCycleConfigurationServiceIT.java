package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link DeviceLifeCycleConfigurationServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (13:12)
 */
public class DeviceLifeCycleConfigurationServiceIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceLifeCycleConfigurationServiceIT.class.getSimpleName());
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
    public void installerCreatedTheDefaultFiniteStateMachine() {
        FiniteStateMachineService finiteStateMachineService = inMemoryPersistence.getService(FiniteStateMachineService.class);

        // Business method: actually the business method is the install method of the DeviceLifeCycleServiceImpl component
        Optional<FiniteStateMachine> stateMachine = finiteStateMachineService
                    .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());

        // Asserts
        assertThat(stateMachine.isPresent()).isTrue();
    }

    @Transactional
    @Test
    public void installerCreatedTheDefaultLifeCycle() {
        DeviceLifeCycleConfigurationServiceImpl service = inMemoryPersistence.getDeviceLifeCycleConfigurationService();

        // Business method: actually the business method is the install method of the DeviceLifeCycleServiceImpl component
        Optional<DeviceLifeCycle> defaultDeviceLifeCycle = service.findDefaultDeviceLifeCycle();

        // Asserts
        assertThat(defaultDeviceLifeCycle.isPresent()).isTrue();
        assertThat(defaultDeviceLifeCycle.get().getName()).isEqualTo(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());
    }

    @Transactional
    @Test
    public void findAllWithOnlyDefault() {
        DeviceLifeCycleConfigurationServiceImpl service = inMemoryPersistence.getDeviceLifeCycleConfigurationService();

        // Business method
        Finder<DeviceLifeCycle> finder = service.findAllDeviceLifeCycles();

        // Asserts
        assertThat(finder).isNotNull();
        assertThat(finder.find()).hasSize(1);
    }

    @Transactional
    @Test
    public void findAllWithDefaultAndThreeOthers() {
        DeviceLifeCycleConfigurationServiceImpl service = inMemoryPersistence.getDeviceLifeCycleConfigurationService();
        FiniteStateMachine stateMachine = this.findDefaultFiniteStateMachine();
        DeviceLifeCycle one = service.newDeviceLifeCycleUsing("One", stateMachine).complete();
        one.save();
        DeviceLifeCycle two = service.newDeviceLifeCycleUsing("Two", stateMachine).complete();
        two.save();
        DeviceLifeCycle three = service.newDeviceLifeCycleUsing("Three", stateMachine).complete();
        three.save();

        // Business method
        Finder<DeviceLifeCycle> finder = service.findAllDeviceLifeCycles();

        // Asserts
        assertThat(finder).isNotNull();
        List<DeviceLifeCycle> deviceLifeCycles = finder.find();
        assertThat(deviceLifeCycles).hasSize(4);
    }

    private FiniteStateMachine findDefaultFiniteStateMachine() {
        return inMemoryPersistence
                .getService(FiniteStateMachineService.class)
                .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finite state machine"));
    }

}