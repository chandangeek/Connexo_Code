package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfo;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCyclePrivilegeInfo;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides factory services for {@link AuthorizedActionEditor}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-26 (15:08)
 */
public class AuthorizedActionEditorFactory {

    private final ResourceHelper resourceHelper;

    public AuthorizedActionEditorFactory(ResourceHelper resourceHelper) {
        super();
        this.resourceHelper = resourceHelper;
    }

    /**
     * Creates a new {@link AuthorizedActionEditor} to save the changes
     * described by the {@link AuthorizedActionInfo} to an existing
     * {@link AuthorizedAction}.
     *
     * @param info The AuthorizedActionInfo
     * @return The AuthorizedActionEditor
     */
    public AuthorizedActionEditor from(DeviceLifeCycle deviceLifeCycle, AuthorizedActionInfo info) {
        AuthorizedAction authorizedAction = this.resourceHelper.findAuthorizedActionByIdOrThrowException(deviceLifeCycle, info.id);
        if (authorizedAction instanceof AuthorizedTransitionAction) {
            AuthorizedTransitionAction transitionAction = (AuthorizedTransitionAction) authorizedAction;
            if (   this.fromStateChanged(transitionAction, info)
                || this.toStateChanged(transitionAction, info)) {
                return new RecreateTransitionAndAction(transitionAction, info);
            }
        }
        return new SimplePropertyEditor(authorizedAction, info);
    }

    private boolean fromStateChanged(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info) {
        return this.stateChanged(transitionAction.getStateTransition().getFrom(), info.fromState.id);
    }

    private boolean toStateChanged(AuthorizedTransitionAction transitionAction, AuthorizedActionInfo info) {
        return this.stateChanged(transitionAction.getStateTransition().getTo(), info.toState.id);
    }

    private boolean stateChanged(State state, long stateId) {
        return state.getId() != stateId;
    }

    private static class SimplePropertyEditor implements AuthorizedActionEditor {
        private final AuthorizedAction target;
        private final AuthorizedActionInfo newAction;

        private SimplePropertyEditor(AuthorizedAction target, AuthorizedActionInfo newAction) {
            super();
            this.target = target;
            this.newAction = newAction;
        }

        @Override
        public AuthorizedAction saveChanges() {
            return this.target;
        }

    }

    private static class RecreateTransitionAndAction implements AuthorizedActionEditor {
        private final AuthorizedTransitionAction obsoleteAction;
        private final AuthorizedActionInfo newAction;
        private FiniteStateMachine finiteStateMachine;
        private FiniteStateMachineUpdater finiteStateMachineUpdater;
        private DeviceLifeCycle deviceLifeCycle;
        private DeviceLifeCycleUpdater deviceLifeCycleUpdater;

        private RecreateTransitionAndAction(AuthorizedTransitionAction obsoleteAction, AuthorizedActionInfo newAction) {
            super();
            this.obsoleteAction = obsoleteAction;
            this.newAction = newAction;
            this.deviceLifeCycle = obsoleteAction.getDeviceLifeCycle();
            this.deviceLifeCycleUpdater = this.deviceLifeCycle.startUpdate();
            this.finiteStateMachine = this.deviceLifeCycle.getFiniteStateMachine();
            this.finiteStateMachineUpdater = this.finiteStateMachine.startUpdate();
        }

        @Override
        public AuthorizedAction saveChanges() {
            this.deviceLifeCycleUpdater.removeTransitionAction(this.obsoleteAction.getStateTransition());
            this.deviceLifeCycle = this.deviceLifeCycleUpdater.complete();
            this.deviceLifeCycle.save();
            StateTransitionEventType eventType = this.obsoleteAction.getStateTransition().getEventType();
            this.finiteStateMachineUpdater
                    .state(this.obsoleteAction.getState().getId())
                    .prohibit(eventType)
                    .complete();
            this.finiteStateMachineUpdater
                    .state(this.newAction.fromState.id)
                    .on(eventType).transitionTo(this.newAction.toState.id)
                    .complete();
            this.finiteStateMachine = this.finiteStateMachineUpdater.complete();
            this.finiteStateMachine.save();
            StateTransition newStateTransition = this.finiteStateMachine.getTransitions().stream().filter(this::isNewTransition).findFirst().get();
            this.deviceLifeCycleUpdater = this.deviceLifeCycle.startUpdate();
            AuthorizedTransitionAction newAction =
                    this.deviceLifeCycleUpdater
                        .newTransitionAction(newStateTransition)
                        .addAllChecks(this.obsoleteAction.getChecks())
                        .addAllActions(this.obsoleteAction.getActions())
                        .addAllLevels(this.toLevels(this.newAction.privileges))
                        .complete();
            this.deviceLifeCycle = this.deviceLifeCycleUpdater.complete();
            this.deviceLifeCycle.save();
            return newAction;
        }

        private boolean isNewTransition(StateTransition mistery) {
            return mistery.getFrom().getId() == this.newAction.fromState.id
                && mistery.getTo().getId() == this.newAction.toState.id;
        }

        private Set<AuthorizedAction.Level> toLevels(List<DeviceLifeCyclePrivilegeInfo> levelInfos) {
            Set<AuthorizedAction.Level> levels = EnumSet.noneOf(AuthorizedAction.Level.class);
            levelInfos
                .stream()
                .map(each -> each.privilege)
                .map(AuthorizedAction.Level::valueOf)
                .forEach(levels::add);
            return levels;
        }
    }

}