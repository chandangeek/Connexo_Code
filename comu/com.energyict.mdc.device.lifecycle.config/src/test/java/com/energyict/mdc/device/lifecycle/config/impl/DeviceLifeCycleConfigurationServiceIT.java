package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.Privileges;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @Test
    public void componentNameIsNotEmpty() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        String componentName = service.getComponentName();

        // Asserts
        assertThat(componentName).isNotEmpty();
    }

    @Test
    public void layerIsNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        Layer layer = service.getLayer();

        // Asserts
        assertThat(layer).isNotNull();
    }

    @Test
    public void prerequisiteModulesNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        List<String> prerequisiteModules = service.getPrerequisiteModules();

        // Asserts
        assertThat(prerequisiteModules).isNotNull();
    }

    @Test
    public void keysNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        List<TranslationKey> keys = service.getKeys();

        // Asserts
        assertThat(keys).isNotNull();
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
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method: actually the business method is the install method of the DeviceLifeCycleServiceImpl component
        Optional<DeviceLifeCycle> defaultDeviceLifeCycle = service.findDefaultDeviceLifeCycle();

        // Asserts
        assertThat(defaultDeviceLifeCycle.isPresent()).isTrue();
        assertThat(defaultDeviceLifeCycle.get().getName()).isEqualTo(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());
    }

    @Transactional
    @Test
    public void findAllWithOnlyDefault() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        Finder<DeviceLifeCycle> finder = service.findAllDeviceLifeCycles();

        // Asserts
        assertThat(finder).isNotNull();
        assertThat(finder.find()).hasSize(1);
    }

    @Transactional
    @Test
    public void findAllWithDefaultAndThreeOthers() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();
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

    @Transactional
    @Test
    public void findPrivilegeThatDoesNotExist() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        Optional<com.elster.jupiter.users.Privilege> doesNotExist = service.findInitiateActionPrivilege("findPrivilegeThatDoesNotExist");

        // Asserts
        assertThat(doesNotExist.isPresent()).isFalse();
    }

    @Transactional
    @Test
    public void findInitiateActionsPrivilege() {
        Stream
            .of(Privileges.INITIATE_ACTION_1, Privileges.INITIATE_ACTION_2, Privileges.INITIATE_ACTION_3, Privileges.INITIATE_ACTION_4)
            .forEach(this::testFindInitiateActionPrivilege);
    }

    @Transactional
    @Test
    public void findOtherPrivilege() {
        Stream
            .of(Privileges.CONFIGURE_DEVICE_LIFE_CYCLE, Privileges.VIEW_DEVICE_LIFE_CYCLE)
            .forEach(this::testShouldNotFindInitiateActionPrivilege);
    }

    @Test
    public void maximumFutureEffectiveTimeShiftIsNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        TimeDuration timeShift = service.getMaximumFutureEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
    }

    @Test
    public void defaultFutureEffectiveTimeShiftIsNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        TimeDuration timeShift = service.getDefaultFutureEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
    }

    @Test
    public void maximumPastEffectiveTimeShiftIsNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        TimeDuration timeShift = service.getMaximumPastEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
    }

    @Test
    public void defaultPastEffectiveTimeShiftIsNotNull() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        TimeDuration timeShift = service.getDefaultPastEffectiveTimeShift();

        // Asserts
        assertThat(timeShift).isNotNull();
    }

    private void testFindInitiateActionPrivilege(String privilegeName) {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        Optional<com.elster.jupiter.users.Privilege> privilege = service.findInitiateActionPrivilege(privilegeName);

        // Asserts
        assertThat(privilege.isPresent()).as("Expecting privilege " + privilegeName + " to exists").isTrue();
    }

    private void testShouldNotFindInitiateActionPrivilege(String privilegeName) {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();

        // Business method
        Optional<com.elster.jupiter.users.Privilege> privilege = service.findInitiateActionPrivilege(privilegeName);

        // Asserts
        assertThat(privilege.isPresent()).as("Not expecting privilege " + privilegeName + " to exist").isFalse();
    }

    private FiniteStateMachine findDefaultFiniteStateMachine() {
        return inMemoryPersistence
                .getService(FiniteStateMachineService.class)
                .findFiniteStateMachineByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey())
                .orElseThrow(() -> new IllegalStateException("Please rerun " + DeviceLifeCycleConfigurationServiceIT.class.getName() + " to find out why the installer has not created the default finite state machine"));
    }

    private DeviceLifeCycleConfigurationServiceImpl getTestInstance() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

}