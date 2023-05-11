/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.fsm.EndPointConfigurationReference;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionChangeEvent;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.StateTransitionTriggerEvent;
import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import com.github.oxo42.stateless4j.StateConfiguration;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
@Component(name = "com.elster.jupiter.fsm.trigger", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class StateTransitionTriggerEventTopicHandler implements TopicHandler {

    private Logger logger = Logger.getLogger(StateTransitionTriggerEventTopicHandler.class.getName());
    private volatile EventService eventService;
    private volatile BpmService bpmService;
    private volatile StateTransitionPropertiesProvider propertiesProvider;
    private volatile FiniteStateMachineService finiteStateMachineService;

    // For OSGi purposes
    public StateTransitionTriggerEventTopicHandler() {
        super();
    }

    // For testing purposes
    @Inject
    public StateTransitionTriggerEventTopicHandler(EventService eventService, BpmService bpmService,
                                                   StateTransitionPropertiesProvider propertiesProvider,
                                                   FiniteStateMachineService finiteStateMachineService) {
        this();
        this.setEventService(eventService);
        this.setBpmService(bpmService);
        this.propertiesProvider = propertiesProvider;
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference
    public void setPropertiesProvider(StateTransitionPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        this.handle((StateTransitionTriggerEvent) localEvent.getSource());
    }

    @Override
    public String getTopicMatcher() {
        return EventType.TRIGGER_EVENT.topic();
    }

    private void handle(StateTransitionTriggerEvent triggerEvent) {
        Optional<State> currentState = triggerEvent.getFiniteStateMachine().getState(triggerEvent.getSourceCurrentStateName());
        if (currentState.isPresent()) {
            this.handle(triggerEvent, currentState.get(), bpmService, propertiesProvider, finiteStateMachineService.getStateTransitionWebServiceClients());
        } else {
            this.logger.fine(ignoreEventMessageSupplier(triggerEvent));
        }
    }

    private Supplier<String> ignoreEventMessageSupplier(StateTransitionTriggerEvent triggerEvent) {
        return () -> "Ignoring event '" + triggerEvent.getType().getSymbol() + "' for finite state machine '" + triggerEvent.getFiniteStateMachine().getName() + "' relating to source object '" + triggerEvent.getSourceId() + "' because current state '" + triggerEvent.getSourceCurrentStateName() + "' does not exist in the finite state machine definition";
    }

    private void handle(StateTransitionTriggerEvent triggerEvent, State currentState, BpmService bpmService,
                        StateTransitionPropertiesProvider propertiesProvider,
                        List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
        this.handle(triggerEvent, new ActualState(currentState), bpmService, propertiesProvider, stateTransitionWebServiceClients);
    }

    private void handle(StateTransitionTriggerEvent triggerEvent, ActualState currentState, BpmService bpmService,
                        StateTransitionPropertiesProvider propertiesProvider,
                        List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
        FiniteStateMachine finiteStateMachine = triggerEvent.getFiniteStateMachine();
        ActualStatesAndTriggers actualStatesAndTriggers = new ActualStatesAndTriggers(finiteStateMachine, triggerEvent.getSourceId(), triggerEvent.getSourceType());
        StateMachineConfig<ActualState, Trigger> stateMachineConfiguration = this.configureStateMachine(actualStatesAndTriggers, bpmService, propertiesProvider, stateTransitionWebServiceClients);
        StateMachine<ActualState, Trigger> stateMachine = new StateMachine<>(currentState, stateMachineConfiguration);
        try {
            stateMachine.fire(actualStatesAndTriggers.get(triggerEvent.getType()));
            ActualState newState = stateMachine.getState();
            if (newState.equals(currentState)) {
                this.logger.fine(() -> "Event '" + triggerEvent.getType().getSymbol() + "' did not cause a state change for source object '" + triggerEvent.getSourceId() + "' because current state '" + triggerEvent.getSourceCurrentStateName() + "'. The latter will remain in state '" + currentState.getName() + "'.");
            } else {
                this.publishChange(currentState, newState, triggerEvent);
                this.executeWebServiceCall(triggerEvent.getSourceId(), triggerEvent.getSourceType(), currentState, newState, triggerEvent.getEffectiveTimestamp());
            }
        } catch (IllegalStateException e) {
            this.logger.fine(() -> "Ignoring event '" + triggerEvent.getType().getSymbol() + "' for finite state machine '" + triggerEvent.getFiniteStateMachine().getName() + "' relating to source object '" + triggerEvent.getSourceId() + "' because it is not allowed for current state '" + triggerEvent.getSourceCurrentStateName());
        }
    }

    private void publishChange(ActualState currentState, ActualState newState, StateTransitionTriggerEvent triggerEvent) {
        StateTransitionChangeEventImpl changeEvent = new StateTransitionChangeEventImpl(this.eventService, currentState.state, newState.state, triggerEvent.getSourceId(), triggerEvent.getSourceType(), triggerEvent.getEffectiveTimestamp(), triggerEvent.getProperties());
        changeEvent.publish();
    }

    private void executeWebServiceCall(String id, String source, ActualState currentState, ActualState newState, Instant effectiveDate) {
        if (source.equals(CallWebServiceClient.DEVICE)) {
            finiteStateMachineService.getStateTransitionWebServiceClients().forEach(stateTransitionWebServiceClient -> {
                String nameService = stateTransitionWebServiceClient.getWebServiceName();

                Set<EndPointConfiguration> endPointConfigurations = currentState.getOnExitEndPointConfigurations().stream()
                        .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                        .filter(endPoint->endPoint.getWebServiceName().equals(nameService))
                        .filter(EndPointConfiguration::isActive)
                        .collect(Collectors.toSet());
                endPointConfigurations.addAll(newState.getOnEntryEndPointConfigurations().stream()
                        .map(EndPointConfigurationReference::getStateChangeEndPointConfiguration)
                        .filter(endPoint->endPoint.getWebServiceName().equals(nameService))
                        .filter(EndPointConfiguration::isActive)
                        .collect(Collectors.toSet()));

                if(!endPointConfigurations.isEmpty()) {
                    stateTransitionWebServiceClient.call(Long.parseLong(id), endPointConfigurations, newState.getName(), effectiveDate);
                }
            });
        }
    }

    private StateMachineConfig<ActualState, Trigger> configureStateMachine(ActualStatesAndTriggers actualStatesAndTriggers,
                                                                           BpmService bpmService,
                                                                           StateTransitionPropertiesProvider propertiesProvider,
                                                                           List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
        StateMachineConfig<ActualState, Trigger> configuration = new StateMachineConfig<>();
        actualStatesAndTriggers.addToConfiguration(configuration, bpmService, propertiesProvider, stateTransitionWebServiceClients);
        return configuration;
    }

    public abstract static class StartExternalProcesses {
        private static final String DEVICE = "com.energyict.mdc.common.device.data.Device";
        private static final String END_DEVICE = "com.elster.jupiter.metering.EndDevice";
        private static final String DEVICE_ASSOCIATION = "device";
        private static final String USAGEPOINT_ASSOCIATION = "usagepoint";
        public static final String USAGEPOINT = "com.elster.jupiter.metering.UsagePoint";
        private static final String PROCESS_KEY_DEVICE_STATES = "deviceStates";
        private static final String AUTH_TYPE = "Bearer ";
        private final List<ProcessReference> processReferences;
        private final String sourceId;
        private final String sourceType;
        private final State state;
        private Logger logger = Logger.getLogger(StateTransitionTriggerEventTopicHandler.class.getName());
        private volatile BpmService bpmService;
        private volatile StateTransitionPropertiesProvider propertiesProvider;

        StartExternalProcesses(BpmService bpmService, StateTransitionPropertiesProvider propertiesProvider, List<ProcessReference> processReferences, String sourceId, State state, String sourceType) {
            super();
            this.sourceId = sourceId;
            this.state = state;
            this.processReferences = new ArrayList<>(processReferences);
            this.bpmService = bpmService;
            this.propertiesProvider = propertiesProvider;
            this.sourceType = sourceType;
        }

        void startAll() {
            this.processReferences.forEach(this::start);
        }

        protected abstract void start(ProcessReference processReference);

        private void logStart(ProcessReference processReference) {
            this.logger.fine(() -> "Starting process with processId: " + processReference.getStateChangeBusinessProcess().getId());
        }

        void startOnEntry(ProcessReference processReference) {
            this.logStart(processReference);
            startProcess(processReference);
        }


        void startOnExit(ProcessReference processReference) {
            this.logStart(processReference);
            startProcess(processReference);
        }

        private void startProcess(ProcessReference processReference) {
            if (sourceType.equals(DEVICE) || sourceType.equals(END_DEVICE)) {
                Optional<BpmProcessDefinition> bpmProcess = bpmService.findBpmProcessDefinition(processReference.getStateChangeBusinessProcess()
                        .getId());
                if (bpmProcess.isPresent() && isProcessAvailableForDeviceTransition(bpmProcess.get())) {
                    Map<String, Object> expectedParams = new HashMap<>();
                    if (sourceType.equals(DEVICE)){
                        expectedParams.put("deviceId", propertiesProvider.getMeterMRID(Long.valueOf(sourceId)));
                    }
                    else if (sourceType.equals(END_DEVICE)){
                        expectedParams.put("deviceId", propertiesProvider.getDeviceMRID(Long.valueOf(sourceId)));
                    }
                    bpmService.startProcess(bpmProcess.get(), expectedParams);
                }
            } else if (sourceType.equals(USAGEPOINT)) {
                Optional<BpmProcessDefinition> bpmProcess = bpmService.findBpmProcessDefinition(processReference.getStateChangeBusinessProcess()
                        .getId());
                if (bpmProcess.isPresent() && isProcessAvailableForUsagePointTransition(bpmProcess.get())) {
                    Map<String, Object> expectedParams = new HashMap<>();
                    expectedParams.put("usagePointId", propertiesProvider.getUsagePointMRID(Long.valueOf(sourceId)));
                    bpmService.startProcess(bpmProcess.get(), expectedParams);
                }
            }
        }

        private boolean isProcessAvailableForUsagePointTransition(BpmProcessDefinition bpmProcess) {
            return bpmProcess.getAssociation().equals(USAGEPOINT_ASSOCIATION) &&
                    propertiesProvider.areProcessPropertiesAvailableForUP(bpmProcess.getProperties(), Long.valueOf(sourceId));
        }

        private boolean isProcessAvailableForDeviceTransition(BpmProcessDefinition bpmProcess) {
            //noinspection unchecked
            return bpmProcess.getAssociation().equals(DEVICE_ASSOCIATION) &&
                    List.class.isInstance(bpmProcess.getProperties().get(PROCESS_KEY_DEVICE_STATES)) &&
                    ((List<Object>) bpmProcess.getProperties().get(PROCESS_KEY_DEVICE_STATES))
                            .stream()
                            .filter(HasIdAndName.class::isInstance)
                            .anyMatch(v -> ((HasIdAndName) v).getId()
                                    .toString()
                                    .equals(String.valueOf(state.getId())));
        }

    }

    private abstract static class CallWebServiceClient {

        private static final String DEVICE = "com.energyict.mdc.common.device.data.Device";
        private final List<EndPointConfigurationReference> endPointConfigurationReferences;
        private final String sourceId;
        private final String sourceType;
        private final State state;
        private final List<StateTransitionWebServiceClient> stateTransitionWebServiceClients;
        private Logger logger = Logger.getLogger(StateTransitionTriggerEventTopicHandler.class.getName());

        CallWebServiceClient(List<StateTransitionWebServiceClient> stateTransitionWebServiceClients,
                             List<EndPointConfigurationReference> endPointConfigurationReferences,
                             String sourceId, State state, String sourceType) {
            super();
            this.sourceId = sourceId;
            this.state = state;
            this.endPointConfigurationReferences = new ArrayList<>(endPointConfigurationReferences);
            this.stateTransitionWebServiceClients = stateTransitionWebServiceClients;
            this.sourceType = sourceType;
        }

        void callAll() {
            this.endPointConfigurationReferences.forEach(this::send);
        }

        protected abstract void send(EndPointConfigurationReference endPointConfigurationReference);

        private void logCall(EndPointConfigurationReference endPointConfigurationReference) {
            this.logger.fine(() -> "Calling web service client with endPointConfigurationId: " + endPointConfigurationReference.getStateChangeEndPointConfiguration().getId());
        }

        void sendOnEntry(EndPointConfigurationReference endPointConfigurationReference) {
            this.logCall(endPointConfigurationReference);
            callWebServiceClient(endPointConfigurationReference);
        }

        void sendOnExit(EndPointConfigurationReference endPointConfigurationReference) {
            this.logCall(endPointConfigurationReference);
            callWebServiceClient(endPointConfigurationReference);
        }

        private void callWebServiceClient(EndPointConfigurationReference endPointConfigurationReference) {
        }
    }

    public static class StartExternalProcessesOnEntry extends StartExternalProcesses {
        StartExternalProcessesOnEntry(BpmService bpmService, StateTransitionPropertiesProvider usagePointProvider, List<ProcessReference> processReferences, String sourceId, State state, String sourceType) {
            super(bpmService, usagePointProvider, processReferences, sourceId, state, sourceType);
        }

        @Override
        protected void start(ProcessReference processReference) {
            this.startOnEntry(processReference);
        }
    }

    public static class StartExternalProcessesOnExit extends StartExternalProcesses {
        StartExternalProcessesOnExit(BpmService bpmService, StateTransitionPropertiesProvider usagePointProvider, List<ProcessReference> processReferences, String sourceId, State state, String sourceType) {
            super(bpmService, usagePointProvider, processReferences, sourceId, state, sourceType);
        }

        @Override
        protected void start(ProcessReference processReference) {
            this.startOnExit(processReference);
        }
    }

    public static class CallWebServiceClientOnEntry extends CallWebServiceClient {
        CallWebServiceClientOnEntry(List<StateTransitionWebServiceClient> stateTransitionWebServiceClients,
                                    List<EndPointConfigurationReference> endPointConfigurationReferences,
                                    String sourceId, State state, String sourceType) {
            super(stateTransitionWebServiceClients, endPointConfigurationReferences, sourceId, state, sourceType);
        }

        @Override
        protected void send(EndPointConfigurationReference endPointConfigurationReference) {
            this.sendOnEntry(endPointConfigurationReference);
        }
    }

    public static class CallWebServiceClientOnExit extends CallWebServiceClient {
        CallWebServiceClientOnExit(List<StateTransitionWebServiceClient> stateTransitionWebServiceClients,
                                   List<EndPointConfigurationReference> endPointConfigurationReferences,
                                   String sourceId, State state, String sourceType) {
            super(stateTransitionWebServiceClients, endPointConfigurationReferences, sourceId, state, sourceType);
        }

        @Override
        protected void send(EndPointConfigurationReference endPointConfigurationReference) {
            this.sendOnExit(endPointConfigurationReference);
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
        public int hashCode() {
            return Long.hashCode(state.getId());
        }

        @Override
        public String toString() {
            return "ActualState(" + this.getName() + ")";
        }

        private List<ProcessReference> getOnEntryProcesses() {
            return this.state.getOnEntryProcesses();
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

        private List<ProcessReference> getOnExitProcesses() {
            return this.state.getOnExitProcesses();
        }

        private List<EndPointConfigurationReference> getOnEntryEndPointConfigurations() {
            return this.state.getOnEntryEndPointConfigurations();
        }

        private List<EndPointConfigurationReference> getOnExitEndPointConfigurations() {
            return this.state.getOnExitEndPointConfigurations();
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
        private final String sourceType;
        private final Map<Long, ActualState> states;
        private final Map<Long, Trigger> triggers;

        private ActualStatesAndTriggers(FiniteStateMachine stateMachine, String sourceId, String sourceType) {
            super();
            this.stateMachine = stateMachine;
            this.sourceId = sourceId;
            this.sourceType = sourceType;
            this.states = stateMachine.getStates().stream()
                    .map(ActualState::new)
                    .collect(Collectors.toMap(ActualState::getId, Function.identity()));
            this.triggers = stateMachine
                    .getTransitions()
                    .stream()
                    .map(StateTransition::getEventType)
                    .map(Trigger::new)
                    .distinct()
                    .collect(Collectors.toMap(Trigger::getId, Function.identity()));
        }

        private ActualState get(State state) {
            return this.states.get(state.getId());
        }

        private Trigger get(StateTransitionEventType eventType) {
            return this.triggers.get(eventType.getId());
        }

        private void addToConfiguration(StateMachineConfig<ActualState, Trigger> configuration, BpmService bpmService,
                                        StateTransitionPropertiesProvider propertiesProvider,
                                        List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
            this.states.values().forEach(s -> this.addToConfiguration(s, configuration, bpmService, propertiesProvider, stateTransitionWebServiceClients));
        }

        private void addToConfiguration(ActualState state, StateMachineConfig<ActualState, Trigger> configuration,
                                        BpmService bpmService, StateTransitionPropertiesProvider propertiesProvider,
                                        List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
            this.addToConfiguration(state, configuration.configure(state), bpmService, propertiesProvider, stateTransitionWebServiceClients);
        }

        private void addToConfiguration(ActualState state, StateConfiguration<ActualState, Trigger> configuration,
                                        BpmService bpmService, StateTransitionPropertiesProvider propertiesProvider,
                                        List<StateTransitionWebServiceClient> stateTransitionWebServiceClients) {
            configuration.onEntry(() -> new StartExternalProcessesOnEntry(bpmService, propertiesProvider, state.getOnEntryProcesses(), this.sourceId, state.state, this.sourceType).startAll());
            configuration.onExit(() -> new StartExternalProcessesOnExit(bpmService, propertiesProvider, state.getOnExitProcesses(), this.sourceId, state.state, this.sourceType).startAll());
            configuration.onEntry(() -> new CallWebServiceClientOnEntry(stateTransitionWebServiceClients, state.getOnEntryEndPointConfigurations(), this.sourceId, state.state, this.sourceType).callAll());
            configuration.onExit(() -> new CallWebServiceClientOnExit(stateTransitionWebServiceClients, state.getOnExitEndPointConfigurations(), this.sourceId, state.state, this.sourceType).callAll());
            state.getOutgoingStateTransitions().forEach(t -> this.addToConfiguration(t, configuration));
        }

        private void addToConfiguration(StateTransition transition, StateConfiguration<ActualState, Trigger> configuration) {
            if (transition.getFrom().getId() != transition.getTo().getId()) {
                configuration.permit(
                        this.get(transition.getEventType()),
                        this.get(transition.getTo()));
            } else {
                // Transition to the same state
                configuration.permitReentry(this.get(transition.getEventType()));
            }
        }
    }
}
