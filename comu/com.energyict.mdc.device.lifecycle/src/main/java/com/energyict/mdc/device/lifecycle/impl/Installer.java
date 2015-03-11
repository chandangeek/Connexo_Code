package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.Privileges;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components of the finate state machine bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:56)
 */
public class Installer {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final UserService userService;
    private final TransactionService transactionService;
    private final FinateStateMachineService stateMachineService;

    public Installer(DataModel dataModel, UserService userService, TransactionService transactionService, FinateStateMachineService stateMachineService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.transactionService = transactionService;
        this.stateMachineService = stateMachineService;
    }

    public void install(boolean transactional, boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }
        this.createPrivileges();
        if (transactional) {
            this.createDefaultFinateStateMachine();
        }
        else {
            this.doCreateDefaultFinateStateMachine();
        }
    }

    private void createPrivileges() {
        this.logger.fine(() -> "Creating privileges");
        this.userService.createResourceWithPrivileges(
                DeviceLifeCycleService.COMPONENT_NAME,
                "deviceLifeCycleAdministration.deviceLifeCycleAdministrations",
                "deviceLifeCycleAdministration.deviceLifeCycleAdministrations.description",
                new String[]{
                        Privileges.CONFIGURE_DEVICE_LIFE_CYCLES,
                        Privileges.VIEW_DEVICE_LIFE_CYCLES});
    }

    private void createDefaultFinateStateMachine() {
        try (TransactionContext context = this.transactionService.getContext()) {
            this.doCreateDefaultFinateStateMachine();
            context.commit();
        }
    }

    private void doCreateDefaultFinateStateMachine() {
        // Create default StateTransitionEventTypes
        this.logger.fine(() -> "Creating default finate state machine transitions...");
        StateTransitionEventType deliveredToWarehouse = this.createNewStateTransitionEventType("#delivered");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType activated = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivated = this.createNewStateTransitionEventType("#deactivated");
        StateTransitionEventType decommissionedEventType = this.createNewStateTransitionEventType("#decommissioned");
        StateTransitionEventType deletedEventType = this.createNewStateTransitionEventType("#deleted");
        this.logger.fine(() -> "Created default finate state machine transitions");

        FinateStateMachineBuilder builder = this.stateMachineService.newFinateStateMachine(DefaultLifeCycleTranslationKey.DEFAULT_FINATE_STATE_MACHINE_NAME.getDefaultFormat());
        // Create default States
        State deleted = builder.newStandardState(DefaultLifeCycleTranslationKey.DELETED_DEFAULT_STATE.getKey()).complete();
        State decommissioned = builder
                .newStandardState(DefaultLifeCycleTranslationKey.DECOMMISSIONED_DEFAULT_STATE.getKey())
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        FinateStateMachineBuilder.StateBuilder activeBuilder = builder.newStandardState(DefaultLifeCycleTranslationKey.ACTIVE_DEFAULT_STATE.getKey());
        FinateStateMachineBuilder.StateBuilder inactiveBuilder = builder.newStandardState(DefaultLifeCycleTranslationKey.INACTIVE_DEFAULT_STATE.getKey());
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned)
                .on(deactivated).transitionTo(inactiveBuilder)
                .complete();
        State inactive = inactiveBuilder
                .on(activated).transitionTo(active)
                .on(decommissionedEventType).transitionTo(decommissioned)
                .complete();
        State commissioned = builder
                .newStandardState(DefaultLifeCycleTranslationKey.COMMISSIONED_DEFAULT_STATE.getKey())
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .complete();
        State inStock = builder
                .newStandardState(DefaultLifeCycleTranslationKey.IN_STOCK_DEFAULT_STATE.getKey())
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        builder
            .newStandardState(DefaultLifeCycleTranslationKey.ORDERED_DEFAULT_STATE.getKey())
            .on(deliveredToWarehouse).transitionTo(inStock)
            .complete();
        FinateStateMachine stateMachine = builder.complete();
        this.logger.fine(() -> "Creating default finate state machine...");
        stateMachine.save();
        this.logger.fine(() -> "Created default finate state machine");
    }

    private StateTransitionEventType createNewStateTransitionEventType(String symbol) {
        StateTransitionEventType commissionedEventType = this.stateMachineService.newCustomStateTransitionEventType(symbol);
        commissionedEventType.save();
        return commissionedEventType;
    }

}