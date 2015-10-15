package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.config.TransitionType;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes the necessary steps to install the technical components
 * of the device life cycle configuration bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:56)
 */
public class Installer {

    public static final String PRIVILEGES_COMPONENT = "MDC";

    private final Logger logger = Logger.getLogger(Installer.class.getName());

    private final DataModel dataModel;
    private final EventService eventService;
    private final UserService userService;
    private final FiniteStateMachineService stateMachineService;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    public Installer(DataModel dataModel, EventService eventService, UserService userService, FiniteStateMachineService stateMachineService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super();
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.userService = userService;
        this.stateMachineService = stateMachineService;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
    }

    public void install(boolean executeDdl) {
        try {
            this.dataModel.install(executeDdl, true);
        }
        catch (Exception e) {
            this.logger.log(Level.SEVERE, e.getMessage(), e);
        }

        this.createEventTypes();
        this.installDefaultLifeCycle();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            try {
                eventType.install(this.eventService);
            } catch (Exception e) {
                this.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }


    private DeviceLifeCycle installDefaultLifeCycle() {
        Map<String, CustomStateTransitionEventType> eventTypes = this.findOrCreateStateTransitionEventTypes();
        return this.createDefaultLifeCycle(
                DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey(),
                eventTypes);
    }

    private Map<String, CustomStateTransitionEventType> findOrCreateStateTransitionEventTypes() {
        // Create default StateTransitionEventTypes
        this.logger.fine(() -> "Finding (or creating) default finite state machine transitions...");
        Map<String, CustomStateTransitionEventType> eventTypes = Stream
            .of(DefaultCustomStateTransitionEventType.values())
            .filter(DefaultCustomStateTransitionEventType::isStandardEventType)
            .map(each -> each.findOrCreate(this.stateMachineService))
            .collect(Collectors.toMap(
                    StateTransitionEventType::getSymbol,
                    Function.identity()));
        this.logger.fine(() -> "Found (or created) default finite state machine transitions");
        return eventTypes;
    }

    public DeviceLifeCycle createDefaultLifeCycle(String name) {
        return this.createDefaultLifeCycle(name, this.findOrCreateStateTransitionEventTypes());
    }

    private DeviceLifeCycle createDefaultLifeCycle(String name, Map<String, CustomStateTransitionEventType> eventTypes) {
        FiniteStateMachine defaultStateMachine = this.createDefaultFiniteStateMachine(name, eventTypes);
        DeviceLifeCycleBuilder builder = this.deviceLifeCycleConfigurationService.newDeviceLifeCycleUsing(name, defaultStateMachine);
        defaultStateMachine
                .getTransitions()
                .stream()
                .forEach(t -> this.addAsAction(t, builder));
        DeviceLifeCycle defaultLifeCycle = builder.complete();
        defaultLifeCycle.save();
        return defaultLifeCycle;
    }

    private FiniteStateMachine createDefaultFiniteStateMachine(String name, Map<String, CustomStateTransitionEventType> eventTypes) {
        StateTransitionEventType commissioningEventType = eventTypes.get(DefaultCustomStateTransitionEventType.COMMISSIONING.getSymbol());
        StateTransitionEventType activated = eventTypes.get(DefaultCustomStateTransitionEventType.ACTIVATED.getSymbol());
        StateTransitionEventType deactivated = eventTypes.get(DefaultCustomStateTransitionEventType.DEACTIVATED.getSymbol());
        StateTransitionEventType decommissionedEventType = eventTypes.get(DefaultCustomStateTransitionEventType.DECOMMISSIONED.getSymbol());
        StateTransitionEventType deletedEventType = eventTypes.get(DefaultCustomStateTransitionEventType.REMOVED.getSymbol());

        FiniteStateMachineBuilder builder = this.stateMachineService.newFiniteStateMachine(name);
        // Create default States
        State removed = builder.newStandardState(DefaultState.REMOVED.getKey()).complete();
        FiniteStateMachineBuilder.StateBuilder inStockBuilder = builder.newStandardState(DefaultState.IN_STOCK.getKey());
        State decommissioned = builder
                .newStandardState(DefaultState.DECOMMISSIONED.getKey())
                .on(deletedEventType).transitionTo(removed, DefaultLifeCycleTranslationKey.TRANSITION_FROM_DECOMMISSIONED_TO_REMOVED)
                .complete();
        FiniteStateMachineBuilder.StateBuilder activeBuilder = builder.newStandardState(DefaultState.ACTIVE.getKey());
        FiniteStateMachineBuilder.StateBuilder inactiveBuilder = builder.newStandardState(DefaultState.INACTIVE.getKey());
        State active = activeBuilder
                .on(decommissionedEventType).transitionTo(decommissioned, DefaultLifeCycleTranslationKey.TRANSITION_FROM_ACTIVE_TO_DECOMMISSIONED)
                .on(deactivated).transitionTo(inactiveBuilder, DefaultLifeCycleTranslationKey.TRANSITION_FROM_ACTIVE_TO_INACTIVE)
                .complete();
        State inactive = inactiveBuilder
                .on(activated).transitionTo(active, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_ACTIVE)
                .on(decommissionedEventType).transitionTo(decommissioned, DefaultLifeCycleTranslationKey.TRANSITION_FROM_INACTIVE_TO_DECOMMISSIONED)
                .complete();
        State commissioning = builder
                .newStandardState(DefaultState.COMMISSIONING.getKey())
                .on(activated).transitionTo(active, DefaultLifeCycleTranslationKey.TRANSITION_FROM_COMMISSIONING_TO_ACTIVE)
                .on(deactivated).transitionTo(inactive, DefaultLifeCycleTranslationKey.TRANSITION_FROM_COMMISSIONING_TO_INACTIVE)
                .complete();
        State inStock = inStockBuilder
                .on(activated).transitionTo(active, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_ACTIVE)
                .on(deactivated).transitionTo(inactive, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_INACTIVE)
                .on(commissioningEventType).transitionTo(commissioning, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_COMMISSIONING)
                .on(deletedEventType).transitionTo(removed, DefaultLifeCycleTranslationKey.TRANSITION_FROM_IN_STOCK_TO_REMOVED)
                .complete();
        this.logger.fine(() -> "Creating default finite state machine...");
        FiniteStateMachine stateMachine = builder.complete(inStock);
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

}