package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles {@link StateTransitionTriggerEvent}s by building the computational
 * model for the related {@link FiniteStateMachine}
 * and triggering the actual event to calculate the new current state.
 * If there is a new current state, that change is then published
 * as a {@link StateTransitionChangeEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-04 (10:40)
 */
@Component(name="com.elster.jupiter.fsm.trigger", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTransitionTriggerEventTopicHandler implements TopicHandler {

    private Logger logger = Logger.getLogger(StateTransitionTriggerEventTopicHandler.class.getName());
    private volatile EventService eventService;

    // For OSGi purposes
    public StateTransitionTriggerEventTopicHandler() {
        super();
    }

    // For testing purposes
    public StateTransitionTriggerEventTopicHandler(EventService eventService) {
        this();
        this.setEventService(eventService);
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.TRIGGER_EVENT.topic();
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((StateTransitionTriggerEvent) localEvent.getSource());
    }

    private void handle(StateTransitionTriggerEvent triggerEvent) {
        Optional<State> currentState = triggerEvent.getFiniteStateMachine().getState(triggerEvent.getSourceCurrentStateName());
        if (currentState.isPresent()) {
            this.handle(triggerEvent, currentState.get());
        }
        else {
            this.logger.fine(ignoreEventMessageSupplier(triggerEvent));
        }
    }

    private Supplier<String> ignoreEventMessageSupplier(StateTransitionTriggerEvent triggerEvent) {
        return () -> "Ignoring event '" + triggerEvent.getType().getSymbol() + "' for finite state machine '" + triggerEvent.getFiniteStateMachine().getName() + "' relating to source object '" + triggerEvent.getSourceId() + "' because current state '" + triggerEvent.getSourceCurrentStateName() + "' does not exist in the finite state machine definition";
    }

    private void handle(StateTransitionTriggerEvent triggerEvent, State currentState) {
        this.handle(triggerEvent, new ActualState(currentState));
    }

    private void handle(StateTransitionTriggerEvent triggerEvent, ActualState currentState) {
        FiniteStateMachine finiteStateMachine = triggerEvent.getFiniteStateMachine();
        ActualStatesAndTriggers actualStatesAndTriggers = new ActualStatesAndTriggers(finiteStateMachine, triggerEvent.getSourceId());
        StateMachineConfig<ActualState, Trigger> stateMachineConfiguration = this.configureStateMachine(actualStatesAndTriggers);
        StateMachine<ActualState, Trigger> stateMachine = new StateMachine<>(currentState, stateMachineConfiguration);
        try {
            stateMachine.fire(actualStatesAndTriggers.get(triggerEvent.getType()));
            ActualState newState = stateMachine.getState();
            if (newState.equals(currentState)) {
                this.logger.fine(() -> "Event '" + triggerEvent.getType().getSymbol() + "' did not cause a state change for source object '" + triggerEvent.getSourceId() + "' because current state '" + triggerEvent.getSourceCurrentStateName() + "'. The latter will remain in state '" + currentState.getName() + "'.");
            }
            else {
                this.publishChange(currentState, newState, triggerEvent);
            }
        }
        catch (IllegalStateException e) {
            this.logger.fine(() -> "Ignoring event '" + triggerEvent.getType().getSymbol() + "' for finite state machine '" + triggerEvent.getFiniteStateMachine().getName() + "' relating to source object '" + triggerEvent.getSourceId() + "' because it is not allowed for current state '" + triggerEvent.getSourceCurrentStateName());
        }
    }

    private void publishChange(ActualState currentState, ActualState newState, StateTransitionTriggerEvent triggerEvent) {
        StateTransitionChangeEventImpl changeEvent = new StateTransitionChangeEventImpl(this.eventService, currentState.state, newState.state, triggerEvent.getSourceId(), triggerEvent.getEffectiveTimestamp(), triggerEvent.getProperties());
        changeEvent.publish();
    }

    private StateMachineConfig<ActualState, Trigger> configureStateMachine(ActualStatesAndTriggers actualStatesAndTriggers) {
        StateMachineConfig<ActualState, Trigger> configuration = new StateMachineConfig<>();
        actualStatesAndTriggers.addToConfiguration(configuration);
        return configuration;
    }

    private abstract static class StartExternalProcesses {
        private Logger logger = Logger.getLogger(StateTransitionTriggerEventTopicHandler.class.getName());
        private final List<ProcessReference> processReferences;
        private final String sourceId;
        private final State state;

        StartExternalProcesses(List<ProcessReference> processReferences, String sourceId, State state) {
            super();
            this.sourceId = sourceId;
            this.state = state;
            this.processReferences = new ArrayList<>(processReferences);
        }

        void startAll() {
            this.processReferences.forEach(this::start);
        }

        protected abstract void start(ProcessReference processReference);

        private void logStart(ProcessReference processReference) {
            this.logger.fine(() -> "Starting process with deploymentId: " + processReference.getStateChangeBusinessProcess()
                    .getDeploymentId() + " and processId: " + processReference.getStateChangeBusinessProcess().getProcessId());
        }

        protected void startOnEntry(ProcessReference processReference) {
            this.logStart(processReference);
            processReference.getStateChangeBusinessProcess().executeOnEntry(this.sourceId, this.state);
        }

        protected void startOnExit(ProcessReference processReference) {
            this.logStart(processReference);
            processReference.getStateChangeBusinessProcess().executeOnExit(this.sourceId, this.state);
        }

    }

    private static class StartExternalProcessesOnEntry extends StartExternalProcesses {
        StartExternalProcessesOnEntry(List<ProcessReference> processReferences, String sourceId, State state) {
            super(processReferences, sourceId, state);
        }

        @Override
        protected void start(ProcessReference processReference) {
            this.startOnEntry(processReference);
        }
    }

    private static class StartExternalProcessesOnExit extends StartExternalProcesses {
        StartExternalProcessesOnExit(List<ProcessReference> processReferences, String sourceId, State state) {
            super(processReferences, sourceId, state);
        }

        @Override
        protected void start(ProcessReference processReference) {
            this.startOnExit(processReference);
        }
    }

    private static class ActualState {
        private final State state;

        private ActualState(State state) {
            super();
            this.state = state;
        }

        private long getId() {
            return this.state.getId();
        }

        private String getName() {
            return this.state.getName();
        }

        private boolean hasName(String name) {
            return this.getName().equals(name);
        }

        @Override
        public String toString() {
            return "ActualState(" + this.getName() + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            ActualState that = (ActualState) other;
            return state.getId() == that.state.getId();
        }

        @Override
        public int hashCode() {
            return Long.hashCode(state.getId());
        }

        private List<ProcessReference> getOnEntryProcesses() {
            return this.state.getOnEntryProcesses();
        }

        private List<ProcessReference> getOnExitProcesses() {
            return this.state.getOnExitProcesses();
        }

        private List<StateTransition> getOutgoingStateTransitions() {
            return this.state.getOutgoingStateTransitions();
        }
    }

    private static class Trigger {
        private final StateTransitionEventType eventType;

        private Trigger(StateTransitionEventType eventType) {
            super();
            this.eventType = eventType;
        }

        private long getId() {
            return this.eventType.getId();
        }

        @Override
        public String toString() {
            return "Trigger(" + this.eventType.getSymbol() + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Trigger trigger = (Trigger) o;
            return eventType.getId() == trigger.eventType.getId();
        }

        @Override
        public int hashCode() {
            return Long.hashCode(eventType.getId());
        }
    }

    /**
     * Maps {@link State}s to {@link ActualState}s and
     * Maps {@link StateTransition}s to {@link Trigger}s.
     * This is to ensure that the same ActualState
     * is used for every unique state in a {@link FiniteStateMachine}
     * and that the same Trigger is used
     * for every unique StateTransition in a FiniteStateMachine.
     */
    private static class ActualStatesAndTriggers {
        private final FiniteStateMachine stateMachine;
        private final String sourceId;
        private final Map<Long, ActualState> states;
        private final Map<Long, Trigger> triggers;

        private ActualStatesAndTriggers(FiniteStateMachine stateMachine, String sourceId) {
            super();
            this.stateMachine = stateMachine;
            this.sourceId = sourceId;
            this.states = stateMachine.getStates().stream()
                    .map(ActualState::new)
                    .collect(Collectors.toMap(
                        ActualState::getId,
                        Function.identity()));
            this.triggers = stateMachine
                    .getTransitions()
                    .stream()
                    .map(StateTransition::getEventType)
                    .map(Trigger::new)
                    .distinct()
                    .collect(Collectors.toMap(
                        Trigger::getId,
                        Function.identity()));
        }

        private ActualState get(State state) {
            return this.states.get(state.getId());
        }

        private Trigger get(StateTransitionEventType eventType) {
            return this.triggers.get(eventType.getId());
        }

        private void addToConfiguration(StateMachineConfig<ActualState, Trigger> configuration) {
            this.states.values().forEach(s -> this.addToConfiguration(s, configuration));
        }

        private void addToConfiguration(ActualState state, StateMachineConfig<ActualState, Trigger> configuration) {
            this.addToConfiguration(state, configuration.configure(state));
        }

        private void addToConfiguration(ActualState state, StateConfiguration<ActualState, Trigger> configuration) {
            configuration.onEntry(() -> new StartExternalProcessesOnEntry(state.getOnEntryProcesses(), this.sourceId, state.state).startAll());
            configuration.onExit(() -> new StartExternalProcessesOnExit(state.getOnExitProcesses(), this.sourceId, state.state).startAll());
            state.getOutgoingStateTransitions().forEach(t -> this.addToConfiguration(t, configuration));
        }

        private void addToConfiguration(StateTransition transition, StateConfiguration<ActualState, Trigger> configuration) {
            if (transition.getFrom().getId() != transition.getTo().getId()) {
                configuration.permit(
                        this.get(transition.getEventType()),
                        this.get(transition.getTo()));
            }
            else {
                // Transition to the same state
                configuration.permitReentry(this.get(transition.getEventType()));
            }
        }
    }

}