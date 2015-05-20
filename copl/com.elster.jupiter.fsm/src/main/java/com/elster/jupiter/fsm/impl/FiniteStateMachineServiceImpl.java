package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.StandardEventPredicate;
import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.streams.Functions;
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
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toMap;

/**
 * Provides an implementation for the {@link FiniteStateMachineService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:50)
 */
@Component(name = "com.elster.jupiter.fsm", service = {FiniteStateMachineService.class, ServerFiniteStateMachineService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + FiniteStateMachineService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class FiniteStateMachineServiceImpl implements ServerFiniteStateMachineService, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile EventService eventService;
    private volatile TransactionService transactionService;
    private volatile List<StandardEventPredicate> standardEventPredicates = new CopyOnWriteArrayList<>();
    private Thesaurus thesaurus;

    // For OSGi purposes
    public FiniteStateMachineServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public FiniteStateMachineServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, EventService eventService, TransactionService transactionService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.setTransactionService(transactionService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            install();
        }
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.userService, eventService).install(true);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(com.elster.jupiter.fsm.MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return FiniteStateMachineService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT");
    }

    @Activate
    public void activate() {
        dataModel.register(this.getModule());
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

    @Override
    @Reference(name = "zStandardEventPredicates", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addStandardEventPredicate(StandardEventPredicate predicate) {
        this.standardEventPredicates.add(predicate);
        this.createStandardEventType(predicate);
    }

    public void removeStandardEventPredicate(StandardEventPredicate predicate) {
        this.standardEventPredicates.remove(predicate);
    }

    @Override
    public String stateTransitionChangeEventTopic() {
        return EventType.CHANGE_EVENT.topic();
    }

    private void createStandardEventType(StandardEventPredicate predicate) {
        try (TransactionContext context = this.transactionService.getContext()) {
            this.createStandardEventType(predicate, this.eventService.getEventTypes());
            context.commit();
        }
    }

    private void createStandardEventType(StandardEventPredicate predicate, List<com.elster.jupiter.events.EventType> allEventTypes) {
        allEventTypes
                .stream()
                .filter(not(com.elster.jupiter.events.EventType::isEnabledForUseInStateMachines))
                .filter(predicate::isCandidate)
                .map(this::newStandardStateTransitionEventType)
                .forEach(StandardStateTransitionEventType::save);
    }

    @Override
    public CustomStateTransitionEventType newCustomStateTransitionEventType(String symbol) {
        return this.dataModel.getInstance(CustomStateTransitionEventTypeImpl.class).initialize(symbol);
    }

    @Override
    public StandardStateTransitionEventType newStandardStateTransitionEventType(com.elster.jupiter.events.EventType eventType) {
        return this.dataModel.getInstance(StandardStateTransitionEventTypeImpl.class).initialize(eventType);
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
    public List<StateTransitionEventType> getStateTransitionEventTypes() {
        return this.dataModel.query(StateTransitionEventType.class).select(Condition.TRUE);
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
        List<Optional<FiniteStateMachine>> allStates = source
            .getStates()
            .stream()
            .map(sourceState -> this.completeCloning(sourceState, stateBuilderMap, builder))
            .filter(Optional::isPresent)
            .collect(Collectors.toList());
        // Exactly one source initial State so allStates will have size 1
        FiniteStateMachine cloned = allStates.get(0).get();
        cloned.save();
        return cloned;
    }

    private Optional<FiniteStateMachine> completeCloning(State sourceState, Map<Long, FiniteStateMachineBuilder.StateBuilder> stateBuilderMap, FiniteStateMachineBuilder builder) {
        FiniteStateMachineBuilder.StateBuilder stateBuilder = stateBuilderMap.get(sourceState.getId());
        State state = stateBuilder.complete();
        if (sourceState.isInitial()) {
            return Optional.of(builder.complete(state));
        }
        else {
            return Optional.empty();
        }
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
        builder.onEntry(processReference.getDeploymentId(), processReference.getProcessId());
    }

    private void cloneOnExitProcess(ProcessReference processReference, FiniteStateMachineBuilder.StateBuilder builder) {
        builder.onExit(processReference.getDeploymentId(), processReference.getProcessId());
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
        Optional<String> customName = source.getName();
        if (customName.isPresent()) {
            builder
                .on(source.getEventType())
                .transitionTo(otherBuilders.get(source.getTo().getId()), customName.get());
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
        return this.dataModel
                .mapper(FiniteStateMachine.class)
                .getUnique(FiniteStateMachineImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public List<FiniteStateMachine> findFiniteStateMachinesUsing(StateTransitionEventType eventType) {
        Condition eventTypeMatches = where(FiniteStateMachineImpl.Fields.TRANSITIONS.fieldName() + "." + StateTransitionImpl.Fields.EVENT_TYPE.fieldName()).isEqualTo(eventType);
        return this.dataModel
                .query(FiniteStateMachine.class, StateTransition.class)
                .select(eventTypeMatches);
    }

}