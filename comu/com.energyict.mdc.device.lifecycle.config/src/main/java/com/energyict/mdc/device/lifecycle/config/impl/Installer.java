package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Takes the necessary steps to install the technical components
 * of the device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:56)
 */
public class Installer {
    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final UserService userService;
    private final TransactionService transactionService;
    private final FiniteStateMachineService stateMachineService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public Installer(DataModel dataModel, UserService userService, TransactionService transactionService, FiniteStateMachineService stateMachineService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.dataModel = dataModel;
        this.userService = userService;
        this.transactionService = transactionService;
        this.stateMachineService = stateMachineService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
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
            this.createDefaultLifeCycle();
        }
        else {
            this.doCreateDefaultLifeCycle();
        }
    }

    private void createPrivileges() {
        this.logger.fine(() -> "Creating privileges");
        this.userService.createResourceWithPrivileges(
                DeviceLifeCycleConfigurationService.COMPONENT_NAME,
                "deviceLifeCycleAdministration.deviceLifeCycleAdministrations",
                "deviceLifeCycleAdministration.deviceLifeCycleAdministrations.description",
                new String[]{
                        Privileges.CONFIGURE_DEVICE_LIFE_CYCLES,
                        Privileges.VIEW_DEVICE_LIFE_CYCLES});
    }

    private DeviceLifeCycle createDefaultLifeCycle() {
        return this.transactionService.execute(this::doCreateDefaultLifeCycle);
    }

    private DeviceLifeCycle doCreateDefaultLifeCycle () {
        FiniteStateMachine defaultStateMachine = this.createDefaultFiniteStateMachine();
        DeviceLifeCycleBuilder builder = this.deviceLifeCycleConfigurationService.newDeviceLifeCycleUsing(
                DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey(),
                defaultStateMachine);
        defaultStateMachine.getTransitions().stream().forEach(t -> this.addAsAction(t, builder));
        DeviceLifeCycle defaultLifeCycle = builder.complete();
        defaultLifeCycle.save();
        return defaultLifeCycle;
    }

    private FiniteStateMachine createDefaultFiniteStateMachine() {
        // Create default StateTransitionEventTypes
        this.logger.fine(() -> "Creating default finite state machine transitions...");
        StateTransitionEventType commissionedEventType = this.createNewStateTransitionEventType("#commissioned");
        StateTransitionEventType activated = this.createNewStateTransitionEventType("#activated");
        StateTransitionEventType deactivated = this.createNewStateTransitionEventType("#deactivated");
        StateTransitionEventType decommissionedEventType = this.createNewStateTransitionEventType("#decommissioned");
        StateTransitionEventType deletedEventType = this.createNewStateTransitionEventType("#deleted");
        this.logger.fine(() -> "Created default finite state machine transitions");

        FiniteStateMachineBuilder builder = this.stateMachineService.newFiniteStateMachine(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());
        // Create default States
        State deleted = builder.newStandardState(DefaultState.DELETED.getKey()).complete();
        State decommissioned = builder
                .newStandardState(DefaultState.DECOMMISSIONED.getKey())
                .on(deletedEventType).transitionTo(deleted)
                .complete();
        FiniteStateMachineBuilder.StateBuilder activeBuilder = builder.newStandardState(DefaultState.ACTIVE.getKey());
        FiniteStateMachineBuilder.StateBuilder inactiveBuilder = builder.newStandardState(DefaultState.INACTIVE.getKey());
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned)
                .on(deactivated).transitionTo(inactiveBuilder)
                .complete();
        State inactive = inactiveBuilder
                .on(activated).transitionTo(active)
                .on(decommissionedEventType).transitionTo(decommissioned)
                .complete();
        State commissioned = builder
                .newStandardState(DefaultState.COMMISSIONED.getKey())
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .complete();
        State inStock = builder
                .newStandardState(DefaultState.IN_STOCK.getKey())
                .on(activated).transitionTo(active)
                .on(deactivated).transitionTo(inactive)
                .on(commissionedEventType).transitionTo(commissioned)
                .complete();
        FiniteStateMachine stateMachine = builder.complete(inStock);
        this.logger.fine(() -> "Creating default finite state machine...");
        stateMachine.save();
        this.logger.fine(() -> "Created default finite state machine");
        return stateMachine;
    }

    private void addAsAction(StateTransition transition, DeviceLifeCycleBuilder builder) {
        builder
            .newTransitionAction(transition)
            .addAllChecks(this.applicableChecksFor(transition))
            .addAllActions(this.applicableActionsFor(transition))
            .addAllLevels(EnumSet.of(AuthorizedAction.Level.ONE, AuthorizedAction.Level.TWO, AuthorizedAction.Level.THREE))
            .complete();
    }

    private Set<MicroCheck> applicableChecksFor(StateTransition transition) {
        return TransitionType.from(transition).get().requiredChecks();
    }

    private Set<MicroAction> applicableActionsFor(StateTransition transition) {
        return TransitionType.from(transition).get().supportedActions();
    }

    private StateTransitionEventType createNewStateTransitionEventType(String symbol) {
        StateTransitionEventType commissionedEventType = this.stateMachineService.newCustomStateTransitionEventType(symbol);
        commissionedEventType.save();
        return commissionedEventType;
    }

}