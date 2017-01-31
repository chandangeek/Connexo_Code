/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.Privileges;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.fsm.StateChangeBusinessProcessInUseException;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.fsm.UnknownStateChangeBusinessProcessException;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.concurrent.DelayedRegistrationHandler;
import com.elster.jupiter.util.concurrent.RegistrationHandler;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.upgrade.InstallIdentifier.identifier;
import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Currying.perform;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toMap;

/**
 * Provides an implementation for the {@link FiniteStateMachineService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:50)
 */
@Component(name = "com.elster.jupiter.fsm", service = {FiniteStateMachineService.class, ServerFiniteStateMachineService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = "name=" + FiniteStateMachineService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class FiniteStateMachineServiceImpl implements ServerFiniteStateMachineService, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile List<StandardEventPredicate> standardEventPredicates = new CopyOnWriteArrayList<>();
    private volatile Thesaurus thesaurus;
    private volatile Publisher publisher;
    private volatile UpgradeService upgradeService;

    private final RegistrationHandler registrationHandler = new DelayedRegistrationHandler();

    // For OSGi purposes
    public FiniteStateMachineServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public FiniteStateMachineServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, EventService eventService, TransactionService transactionService, Publisher publisher, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setEventService(eventService);
        setTransactionService(transactionService);
        setPublisher(publisher);
        setUpgradeService(upgradeService);
        this.activate();
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public String getComponentName() {
        return FiniteStateMachineService.COMPONENT_NAME;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Activate
    public void activate() {
        dataModel.register(this.getModule());
        upgradeService.register(identifier("Pulse", COMPONENT_NAME), dataModel, Installer.class, ImmutableMap.of(
                version(10, 2), UpgraderV10_2.class
        ));
        registrationHandler.ready();
    }

    // For integration testing components only
    DataModel getDataModel() {
        return dataModel;
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(NlsService.class).toInstance(nlsService);
                bind(UserService.class).toInstance(userService);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(Publisher.class).toInstance(publisher);

                bind(FiniteStateMachineService.class).toInstance(FiniteStateMachineServiceImpl.this);
                bind(ServerFiniteStateMachineService.class).toInstance(FiniteStateMachineServiceImpl.this);
            }
        };
    }

    @Reference(name = "theOrmService")
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(FiniteStateMachineService.COMPONENT_NAME, "Finite State Machine");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference(name = "theNlsService")
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(FiniteStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference(name = "theUserService")
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference(name = "theEventService")
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference(name = "theTransactionService")
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference(name = "thePublisher")
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public List<StateChangeBusinessProcess> findStateChangeBusinessProcesses() {
        return this.dataModel.mapper(StateChangeBusinessProcess.class).find();
    }

    @Override
    public Optional<StateChangeBusinessProcess> findStateChangeBusinessProcessById(long id) {
        return this.dataModel.mapper(StateChangeBusinessProcess.class).getOptional(id);
    }

    @Override
    public StateChangeBusinessProcess enableAsStateChangeBusinessProcess(String name, String deploymentId, String processId) {
        StateChangeBusinessProcessImpl businessProcess = this.dataModel.getInstance(StateChangeBusinessProcessImpl.class).initialize(name, deploymentId, processId);
        Save.CREATE.validate(this.dataModel, businessProcess);
        this.dataModel.persist(businessProcess);
        return businessProcess;
    }

    @Override
    public void disableAsStateChangeBusinessProcess(String deploymentId, String processId) {
        List<StateChangeBusinessProcess> businessProcesses = this.dataModel
                .mapper(StateChangeBusinessProcess.class)
                .find(
                        StateChangeBusinessProcessImpl.Fields.DEPLOYMENT_ID.fieldName(), deploymentId,
                        StateChangeBusinessProcessImpl.Fields.PROCESS_ID.fieldName(), processId);
        if (businessProcesses.isEmpty()) {
            throw new UnknownStateChangeBusinessProcessException(this.thesaurus, deploymentId, processId);
        }
        else {
            Condition condition = where(ProcessReferenceImpl.Fields.PROCESS.fieldName()).in(businessProcesses);
            List<ProcessReference> processReferences = this.dataModel.mapper(ProcessReference.class).select(condition);
            if (processReferences.isEmpty()) {
                businessProcesses.stream().forEach(this.dataModel::remove);
            }
            else {
                throw new StateChangeBusinessProcessInUseException(this.thesaurus, businessProcesses.get(0));
            }
        }
    }

    @Override
    @Reference(name = "zStandardEventPredicates", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStandardEventPredicate(StandardEventPredicate predicate) {
        registrationHandler.handle(perform(this::doRegister).on(predicate));
    }

    private void doRegister(StandardEventPredicate predicate) {
        this.standardEventPredicates.add(predicate);
        if (transactionService.isInTransaction()) {
            createStandardEventTypeNoTransaction(predicate);
        } else {
            createStandardEventType(predicate);
        }
    }

    public void removeStandardEventPredicate(StandardEventPredicate predicate) {
        this.standardEventPredicates.remove(predicate);
    }

    @Override
    public String stateTransitionChangeEventTopic() {
        return EventType.CHANGE_EVENT.topic();
    }

    private void createStandardEventType(StandardEventPredicate predicate) {
        transactionService
                .builder()
                .principal(() -> "Fsm Install")
                .run(() -> createStandardEventTypeNoTransaction(predicate));
    }

    private void createStandardEventTypeNoTransaction(StandardEventPredicate predicate) {
        this.createStandardEventType(predicate, this.eventService.getEventTypes());
    }

    private void createStandardEventType(StandardEventPredicate predicate, List<com.elster.jupiter.events.EventType> allEventTypes) {
        allEventTypes
                .stream()
                .filter(not(com.elster.jupiter.events.EventType::isEnabledForUseInStateMachines))
                .filter(predicate::isCandidate)
                .forEach(this::newStandardStateTransitionEventType);
    }

    @Override
    public CustomStateTransitionEventType newCustomStateTransitionEventType(String symbol, String context) {
        CustomStateTransitionEventTypeImpl eventType = this.dataModel.getInstance(CustomStateTransitionEventTypeImpl.class)
                .initialize(symbol, context);
        eventType.save();
        return eventType;
    }

    @Override
    public StandardStateTransitionEventType newStandardStateTransitionEventType(com.elster.jupiter.events.EventType eventType) {
        StandardStateTransitionEventTypeImpl stateTransitionEventType = this.dataModel.getInstance(StandardStateTransitionEventTypeImpl.class).initialize(eventType);
        stateTransitionEventType.save();
        return stateTransitionEventType;
    }

    @Override
    public Optional<CustomStateTransitionEventType> findCustomStateTransitionEventType(String symbol) {
        return this.dataModel
                .mapper(CustomStateTransitionEventType.class)
                .getUnique(StateTransitionEventTypeImpl.Fields.SYMBOL.fieldName(), symbol);
    }

    @Override
    public Optional<StandardStateTransitionEventType> findStandardStateTransitionEventType(com.elster.jupiter.events.EventType eventType) {
        return this.dataModel
                .mapper(StandardStateTransitionEventType.class)
                .getUnique(StateTransitionEventTypeImpl.Fields.EVENT_TYPE.fieldName(), eventType);
    }

    @Override
    public Optional<StateTransitionEventType> findStateTransitionEventTypeBySymbol(String symbol) {
        Condition custom = where("class").isEqualTo(StateTransitionEventTypeImpl.CUSTOM)
                .and(where(StateTransitionEventTypeImpl.Fields.SYMBOL.fieldName()).isEqualTo(symbol));
        Condition standard = where("class").isEqualTo(StateTransitionEventTypeImpl.STANDARD)
                .and(where(StateTransitionEventTypeImpl.Fields.EVENT_TYPE.fieldName() + ".topic").isEqualTo(symbol));
        List<StateTransitionEventType> eventTypes = this.dataModel.query(StateTransitionEventType.class, com.elster.jupiter.events.EventType.class).select(custom.or(standard));
        if (eventTypes.isEmpty()) {
            return Optional.empty();
        }
        else if (eventTypes.size() > 1) {
            throw new NotUniqueException(symbol);
        }
        else {
            return Optional.of(eventTypes.get(0));
        }
    }

    @Override
    public List<StateTransitionEventType> getStateTransitionEventTypes(String... context) {
        Condition condition = context.length == 0
                ? Condition.TRUE
                : Where.where(StateTransitionEventTypeImpl.Fields.CONTEXT.fieldName()).in(Arrays.asList(context));
        return this.dataModel.query(StateTransitionEventType.class).select(condition);
    }

    @Override
    public FiniteStateMachineBuilder newFiniteStateMachine(String name) {
        FiniteStateMachineImpl stateMachine = this.dataModel.getInstance(FiniteStateMachineImpl.class).initialize(name);
        return new FiniteStateMachineBuilderImpl(dataModel, stateMachine);
    }

    @Override
    public FiniteStateMachine cloneFiniteStateMachine(FiniteStateMachine source, String name) {
        FiniteStateMachineBuilder builder = this.newFiniteStateMachine(name);
        Map<Long, FiniteStateMachineBuilder.StateBuilder> stateBuilderMap = this.cloneStateAndTransitions(source, builder);
        List<State> initialState = source.getStates().stream()
                .map(sourceState -> this.completeCloning(sourceState, stateBuilderMap, builder))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());//expect only one initial state
        return builder.complete(initialState.get(0));
    }

    private Optional<State> completeCloning(State sourceState, Map<Long, FiniteStateMachineBuilder.StateBuilder> stateBuilderMap, FiniteStateMachineBuilder builder) {
        FiniteStateMachineBuilder.StateBuilder stateBuilder = stateBuilderMap.get(sourceState.getId());
        State state = stateBuilder.complete();
        return sourceState.isInitial() ? Optional.of(state) : Optional.empty();
    }

    private Map<Long, FiniteStateMachineBuilder.StateBuilder> cloneStateAndTransitions(FiniteStateMachine source, FiniteStateMachineBuilder builder) {
        Map<Long, FiniteStateMachineBuilder.StateBuilder> stateBuilderMap = source.getStates()
                .stream()
                .collect(toMap(
                        State::getId,
                        state -> this.cloneState(state, builder)));
        stateBuilderMap.forEach(
                (sourceStateId, clonedStateBuilder) -> this.cloneTransitions(source, sourceStateId, clonedStateBuilder, stateBuilderMap));
        return stateBuilderMap;
    }

    private FiniteStateMachineBuilder.StateBuilder cloneState(State source, FiniteStateMachineBuilder builder) {
        FiniteStateMachineBuilder.StateBuilder stateBuilder = this.startCloning(source, builder);
        this.cloneProcesses(source, stateBuilder);
        return stateBuilder;
    }

    private FiniteStateMachineBuilder.StateBuilder startCloning(State source, FiniteStateMachineBuilder builder) {
        if (source.isCustom()) {
            return builder.newCustomState(source.getName());
        }
        else {
            return builder.newStandardState(source.getName());
        }
    }

    private void cloneProcesses(State source, FiniteStateMachineBuilder.StateBuilder builder) {
        source.getOnEntryProcesses().stream().forEach(p -> this.cloneOnEntryProcess(p, builder));
        source.getOnExitProcesses().stream().forEach(p -> this.cloneOnExitProcess(p, builder));
    }

    private void cloneOnEntryProcess(ProcessReference processReference, FiniteStateMachineBuilder.StateBuilder builder) {
        builder.onEntry(processReference.getStateChangeBusinessProcess());
    }

    private void cloneOnExitProcess(ProcessReference processReference, FiniteStateMachineBuilder.StateBuilder builder) {
        builder.onExit(processReference.getStateChangeBusinessProcess());
    }

    private void cloneTransitions(FiniteStateMachine source, long sourceStateId, FiniteStateMachineBuilder.StateBuilder builder, Map<Long, FiniteStateMachineBuilder.StateBuilder> otherBuilders) {
        this.cloneTransitions(
                source.getStates().stream().filter(s -> s.getId() == sourceStateId).findFirst().get(),
                builder,
                otherBuilders);
    }

    private void cloneTransitions(State source, FiniteStateMachineBuilder.StateBuilder builder, Map<Long, FiniteStateMachineBuilder.StateBuilder> otherBuilders) {
        source.getFiniteStateMachine()
                .getTransitions()
                .stream()
                .filter(t -> t.getFrom().getId() == source.getId())
                .forEach(t -> this.cloneTransition(t, builder, otherBuilders));
    }

    private void cloneTransition(StateTransition source, FiniteStateMachineBuilder.StateBuilder builder, Map<Long, FiniteStateMachineBuilder.StateBuilder> otherBuilders) {
        if (source.getName().isPresent()) {
            builder
                .on(source.getEventType())
                .transitionTo(otherBuilders.get(source.getTo().getId()), source.getName().get());
        }
        else if (source.getTranslationKey().isPresent()) {
            builder
                .on(source.getEventType())
                .transitionTo(otherBuilders.get(source.getTo().getId()), new TranslationKeyFromString(source.getTranslationKey().get()));
        }
        else {
            builder
                .on(source.getEventType())
                .transitionTo(otherBuilders.get(source.getTo().getId()));
        }
    }

    @Override
    public Optional<FiniteStateMachine> findFiniteStateMachineById(long id) {
        return this.dataModel.mapper(FiniteStateMachine.class).getOptional(id);
    }

    @Override
    public Optional<FiniteStateMachine> findFiniteStateMachineByName(String name) {
        Condition condition =     where(FiniteStateMachineImpl.Fields.NAME.fieldName()).isEqualTo(name)
                             .and(where(FiniteStateMachineImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).isNull());
        List<FiniteStateMachine> finiteStateMachines = this.dataModel.query(FiniteStateMachine.class).select(condition);
        // Expecting at most one
        if (finiteStateMachines.isEmpty()) {
            return Optional.empty();
        }
        else if (finiteStateMachines.size() > 1) {
            throw new NotUniqueException(name);
        }
        else {
            return Optional.of(finiteStateMachines.get(0));
        }
    }

    @Override
    public List<FiniteStateMachine> findFiniteStateMachinesUsing(StateTransitionEventType eventType) {
        Condition eventTypeMatches = where(FiniteStateMachineImpl.Fields.TRANSITIONS.fieldName() + "." + StateTransitionImpl.Fields.EVENT_TYPE.fieldName()).isEqualTo(eventType);
        return this.dataModel
                .query(FiniteStateMachine.class, StateTransition.class)
                .select(eventTypeMatches);
    }

    @Override
    public Optional<State> findFiniteStateById(long id) {
        return this.dataModel.mapper(State.class).getOptional(id);
    }

    @Override
    public Optional<State> findAndLockStateByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(State.class).lockObjectIfVersion(version, id);
    }

    private class TranslationKeyFromString implements TranslationKey {
        private final String key;
        private TranslationKeyFromString(String key) {
            super();
            this.key = key;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getDefaultFormat() {
            return this.key;
        }
    }
}