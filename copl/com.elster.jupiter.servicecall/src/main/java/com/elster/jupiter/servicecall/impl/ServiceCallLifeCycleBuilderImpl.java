/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.servicecall.CannotRemoveStateException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.NoPathLeftToSuccessFromStateException;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.UnreachableStateException;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.graph.DiGraph;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.CREATED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.ONGOING;
import static com.elster.jupiter.servicecall.DefaultState.PARTIAL_SUCCESS;
import static com.elster.jupiter.servicecall.DefaultState.PAUSED;
import static com.elster.jupiter.servicecall.DefaultState.PENDING;
import static com.elster.jupiter.servicecall.DefaultState.REJECTED;
import static com.elster.jupiter.servicecall.DefaultState.SCHEDULED;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.elster.jupiter.servicecall.DefaultState.WAITING;
import static com.elster.jupiter.util.streams.Predicates.not;

public class ServiceCallLifeCycleBuilderImpl implements ServiceCallLifeCycleBuilder {

    private final DiGraph<DefaultState> graph = buildGraph();
    private final Map<Pair<DefaultState, DefaultState>, TranslationKey> translations = transitionTranslations();
    private final FiniteStateMachineService finiteStateMachineService;
    private final Set<DefaultState> criticalStates = EnumSet.of(CREATED, PENDING, ONGOING, SUCCESSFUL, FAILED);
    private final Provider<ServiceCallLifeCycleImpl> serviceCallLifeCycleFactory;
    private final Thesaurus thesaurus;

    private String name;

    @Inject
    ServiceCallLifeCycleBuilderImpl(FiniteStateMachineService finiteStateMachineService, Provider<ServiceCallLifeCycleImpl> serviceCallLifeCycleFactory, Thesaurus thesaurus) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.serviceCallLifeCycleFactory = serviceCallLifeCycleFactory;
        this.thesaurus = thesaurus;
    }

    ServiceCallLifeCycleBuilder setName(String name) {
        this.name = name;
        return this;
    }

    private DiGraph<DefaultState> buildGraph() {
        DiGraph<DefaultState> graph = new DiGraph<>();
        graph.addEdge(CREATED, SCHEDULED);
        graph.addEdge(CREATED, REJECTED);
        graph.addEdge(CREATED, PENDING);
        graph.addEdge(CREATED, CANCELLED);
        graph.addEdge(SCHEDULED, CANCELLED);
        graph.addEdge(SCHEDULED, PENDING);
        graph.addEdge(PENDING, CANCELLED);
        graph.addEdge(PENDING, ONGOING);
        graph.addEdge(ONGOING, PARTIAL_SUCCESS);
        graph.addEdge(ONGOING, FAILED);
        graph.addEdge(ONGOING, SUCCESSFUL);
        graph.addEdge(ONGOING, CANCELLED);
        graph.addEdge(ONGOING, PAUSED);
        graph.addEdge(ONGOING, WAITING);
        graph.addEdge(PARTIAL_SUCCESS, SCHEDULED);
        graph.addEdge(FAILED, SCHEDULED);
        graph.addEdge(PAUSED, ONGOING);
        graph.addEdge(PAUSED, CANCELLED);
        graph.addEdge(WAITING, ONGOING);
        graph.addEdge(WAITING, CANCELLED);
        return graph;
    }

    private Map<Pair<DefaultState, DefaultState>, TranslationKey> transitionTranslations() {
        Map<Pair<DefaultState, DefaultState>, TranslationKey> map = new HashMap<>();
        map.put(Pair.of(CREATED, SCHEDULED), TranslationKeys.TRANSITION_FROM_CREATED_TO_SCHEDULED);
        map.put(Pair.of(CREATED, REJECTED), TranslationKeys.TRANSITION_FROM_CREATED_TO_REJECTED);
        map.put(Pair.of(CREATED, PENDING), TranslationKeys.TRANSITION_FROM_CREATED_TO_PENDING);
        map.put(Pair.of(CREATED, CANCELLED), TranslationKeys.TRANSITION_FROM_CREATED_TO_CANCELLED);
        map.put(Pair.of(SCHEDULED, CANCELLED), TranslationKeys.TRANSITION_FROM_SCHEDULED_TO_CANCELLED);
        map.put(Pair.of(SCHEDULED, PENDING), TranslationKeys.TRANSITION_FROM_SCHEDULED_TO_PENDING);
        map.put(Pair.of(PENDING, CANCELLED), TranslationKeys.TRANSITION_FROM_PENDING_TO_CANCELLED);
        map.put(Pair.of(PENDING, ONGOING), TranslationKeys.TRANSITION_FROM_PENDING_TO_ONGOING);
        map.put(Pair.of(ONGOING, PARTIAL_SUCCESS), TranslationKeys.TRANSITION_FROM_ONGOING_TO_PARTIAL_SUCCESS);
        map.put(Pair.of(ONGOING, FAILED), TranslationKeys.TRANSITION_FROM_ONGOING_TO_FAILED);
        map.put(Pair.of(ONGOING, SUCCESSFUL), TranslationKeys.TRANSITION_FROM_ONGOING_TO_SUCCESSFUL);
        map.put(Pair.of(ONGOING, CANCELLED), TranslationKeys.TRANSITION_FROM_ONGOING_TO_CANCELLED);
        map.put(Pair.of(ONGOING, PAUSED), TranslationKeys.TRANSITION_FROM_ONGOING_TO_PAUSED);
        map.put(Pair.of(ONGOING, WAITING), TranslationKeys.TRANSITION_FROM_ONGOING_TO_WAITING);
        map.put(Pair.of(PARTIAL_SUCCESS, SCHEDULED), TranslationKeys.TRANSITION_FROM_PARTIAL_SUCCESS_TO_SCHEDULED);
        map.put(Pair.of(FAILED, SCHEDULED), TranslationKeys.TRANSITION_FROM_FAILED_TO_SCHEDULED);
        map.put(Pair.of(PAUSED, ONGOING), TranslationKeys.TRANSITION_FROM_PAUSED_TO_ONGOING);
        map.put(Pair.of(PAUSED, CANCELLED), TranslationKeys.TRANSITION_FROM_PAUSED_TO_CANCELLED);
        map.put(Pair.of(WAITING, ONGOING), TranslationKeys.TRANSITION_FROM_WAITING_TO_ONGOING);
        map.put(Pair.of(WAITING, CANCELLED), TranslationKeys.TRANSITION_FROM_WAITING_TO_CANCELLED);
        return map;
    }

    @Override
    public ServiceCallLifeCycleBuilder remove(DefaultState state) {
        if (criticalStates.contains(state)) {
            throw new CannotRemoveStateException(thesaurus, MessageSeeds.CANNOT_REMOVE_STATE_EXCEPTION, state);
        }
        graph.remove(state);
        return this;
    }

    @Override
    public ServiceCallLifeCycleBuilder removeTransition(DefaultState from, DefaultState to) {
        graph.removeEdge(from, to);
        return this;
    }

    @Override
    public ServiceCallLifeCycle create() {
        validateGraph();

        FiniteStateMachineBuilder builder = finiteStateMachineService.newFiniteStateMachine(name);

        Map<DefaultCustomStateTransitionEventType, CustomStateTransitionEventType> transitions = transitions();

        Map<DefaultState, FiniteStateMachineBuilder.StateBuilder> stateBuilders = graph.vertices()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        state -> builder.newStandardState(state.getKey()),
                        (builder1, builder2) -> builder1,
                        () -> new EnumMap<>(DefaultState.class)
                ));
        graph.edges()
                .forEach(edge -> {
                    FiniteStateMachineBuilder.StateBuilder fromStateBuilder = stateBuilders.get(edge.from());
                    FiniteStateMachineBuilder.StateBuilder toStateBuilder = stateBuilders.get(edge.to());

                    Map.Entry<DefaultCustomStateTransitionEventType, CustomStateTransitionEventType> transition = transitions
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().getTarget().equals(edge.to()))
                            .findAny()
                            .get();

                    fromStateBuilder.on(transition.getValue())
                            .transitionTo(toStateBuilder, translations.get(Pair.of(edge.from(), edge.to())));
                });
        State initialState = stateBuilders.get(DefaultState.CREATED).complete();
        List<State> states = stateBuilders.entrySet()
                .stream()
                .filter(entry -> !DefaultState.CREATED.equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(FiniteStateMachineBuilder.StateBuilder::complete)
                .collect(Collectors.toList());
        FiniteStateMachine finiteStateMachine = builder.complete(initialState);
        ServiceCallLifeCycleImpl serviceCallLifeCycle = serviceCallLifeCycleFactory.get();
        serviceCallLifeCycle.init(name, finiteStateMachine);
        serviceCallLifeCycle.save();
        return serviceCallLifeCycle;
    }

    private void validateGraph() {
        graph.vertices()
                .stream()
                .filter(not(this::hasPathToSuccess))
                .findAny()
                .ifPresent(stuckState -> {
                    throw new NoPathLeftToSuccessFromStateException(thesaurus, MessageSeeds.NO_PATH_TO_SUCCESS_FROM, stuckState);
                });
        graph.vertices()
                .stream()
                .filter(not(this::hasPathFromCreated))
                .findAny()
                .ifPresent(unreachable -> {
                    throw new UnreachableStateException(thesaurus, MessageSeeds.NO_PATH_FROM_CREATED_TO, unreachable);
                });

    }

    private boolean hasPathToSuccess(DefaultState state) {
        // check there is a path from each state to an end state
        Set<DefaultState> endStates = EnumSet.of(CANCELLED, REJECTED, SUCCESSFUL, FAILED, PARTIAL_SUCCESS);
        if (endStates.contains(state)) {
            return true;
        }
        endStates.retainAll(graph.vertices());
        return graph.shortestPath(state, SUCCESSFUL).isPresent();
    }

    private boolean hasPathFromCreated(DefaultState state) {
        // check there is a path from each state to an end state
        if (CREATED.equals(state)) {
            return true;
        }
        return graph.shortestPath(CREATED, state).isPresent();
    }

    private Map<DefaultCustomStateTransitionEventType, CustomStateTransitionEventType> transitions() {
        return Arrays.stream(DefaultCustomStateTransitionEventType.values())
                .collect(Collectors.toMap(
                        Function.<DefaultCustomStateTransitionEventType>identity(),
                        transitionType -> transitionType.findOrCreate(finiteStateMachineService)
                ));
    }

}
