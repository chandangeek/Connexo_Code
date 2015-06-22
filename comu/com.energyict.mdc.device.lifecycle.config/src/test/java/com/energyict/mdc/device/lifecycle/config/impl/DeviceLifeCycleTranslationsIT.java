package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.nls.impl.NlsServiceImpl;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;

import org.junit.*;
import org.junit.rules.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Integration test for the translations used by the
 * {@link DeviceLifeCycleConfigurationServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (13:12)
 */
public class DeviceLifeCycleTranslationsIT {

    private static InMemoryPersistence inMemoryPersistence;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceLifeCycleTranslationsIT.class.getSimpleName());
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
    public void installerUsedCorrectTranslationsForTransitions() {
        DeviceLifeCycleConfigurationServiceImpl service = this.getTestInstance();
        NlsServiceImpl nlsService = inMemoryPersistence.getService(NlsServiceImpl.class);
        nlsService.addTranslationProvider(service);

        // Business method: actually the business method is the install method of the DeviceLifeCycleServiceImpl component
        DeviceLifeCycle defaultDeviceLifeCycle = service.findDefaultDeviceLifeCycle().get();

        // Asserts
        defaultDeviceLifeCycle.getAuthorizedActions().forEach(this::assertTranslation);
    }

    private void assertTranslation(AuthorizedAction authorizedAction) {
        this.assertTranslation((AuthorizedTransitionAction) authorizedAction);
    }

    private void assertTranslation(AuthorizedTransitionAction action) {
        if (DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol().equals(action.getStateTransition().getEventType().getSymbol())) {
            this.assertActivatedTransitionTranslated(action);
        }
        else if (DefaultCustomStateTransitionEventType.COMMISSIONING.getSymbol().equals(action.getStateTransition().getEventType().getSymbol())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING.getDefaultFormat());
        }
        else if (DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol().equals(action.getStateTransition().getEventType().getSymbol())) {
            this.assertDeactivatedTransitionTranslated(action);
        }
        else if (DefaultCustomStateTransitionEventType.DECOMMISSIONED.getSymbol().equals(action.getStateTransition().getEventType().getSymbol())) {
            this.assertDecommissionedTransitionTranslated(action);
        }
        else if (DefaultCustomStateTransitionEventType.REMOVED.getSymbol().equals(action.getStateTransition().getEventType().getSymbol())) {
            this.assertRemovedTransitionTranslated(action);
        }
        else {
            fail("Unexpected state transition event type " + action.getStateTransition().getEventType().getSymbol());
        }
    }

    private void assertActivatedTransitionTranslated(AuthorizedTransitionAction action) {
        if (DefaultState.IN_STOCK.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_ACTIVE.getDefaultFormat());
        }
        else if (DefaultState.COMMISSIONING.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_COMMISSIONING_TO_ACTIVE.getDefaultFormat());
        }
        else if (DefaultState.INACTIVE.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_ACTIVE.getDefaultFormat());
        }
        else {
            fail("Unexpected state transition event type " + action.getStateTransition().getEventType().getSymbol());
        }
    }

    private void assertDeactivatedTransitionTranslated(AuthorizedTransitionAction action) {
        if (DefaultState.IN_STOCK.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_INACTIVE.getDefaultFormat());
        }
        else if (DefaultState.COMMISSIONING.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_COMMISSIONING_TO_INACTIVE.getDefaultFormat());
        }
        else if (DefaultState.ACTIVE.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_ACTIVE_TO_INACTIVE.getDefaultFormat());
        }
        else {
            fail("Unexpected state transition event type " + action.getStateTransition().getEventType().getSymbol());
        }
    }

    private void assertDecommissionedTransitionTranslated(AuthorizedTransitionAction action) {
        if (DefaultState.ACTIVE.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_ACTIVE_TO_DECOMMISSIONED.getDefaultFormat());
        }
        else if (DefaultState.INACTIVE.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED.getDefaultFormat());
        }
        else {
            fail("Unexpected state transition event type " + action.getStateTransition().getEventType().getSymbol());
        }
    }

    private void assertRemovedTransitionTranslated(AuthorizedTransitionAction action) {
        if (DefaultState.IN_STOCK.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_REMOVED.getDefaultFormat());
        }
        else if (DefaultState.DECOMMISSIONED.getKey().equals(action.getState().getName())) {
            assertThat(action.getName()).isEqualTo(DefaultLifeCycleTranslationKey.TRANSITION_FROM_DECOMMISSIONED_TO_REMOVED.getDefaultFormat());
        }
        else {
            fail("Unexpected state transition event type " + action.getStateTransition().getEventType().getSymbol());
        }
    }

    private DeviceLifeCycleConfigurationServiceImpl getTestInstance() {
        return inMemoryPersistence.getDeviceLifeCycleConfigurationService();
    }

}