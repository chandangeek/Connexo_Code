package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.usagepoint.lifecycle.config.DefaultState;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;
import com.elster.jupiter.usagepoint.lifecycle.config.Privileges;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleBuilder;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroActionFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointMicroCheckFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Predicates.not;

@Component(name = "UsagePointLifeCycleConfigurationServiceImpl",
        service = {UsagePointLifeCycleConfigurationService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true)
public class UsagePointLifeCycleConfigurationServiceImpl implements UsagePointLifeCycleConfigurationService, MessageSeedProvider, TranslationKeyProvider {
    private static final String FSM_NAME_PREFIX = UsagePointLifeCycleConfigurationService.COMPONENT_NAME + "_";

    private DataModel dataModel;
    private Thesaurus thesaurus;
    private UpgradeService upgradeService;
    private UserService userService;
    private FiniteStateMachineService stateMachineService;
    private EventService eventService;
    private List<UsagePointMicroActionFactory> microActionFactories = new CopyOnWriteArrayList<>();
    private List<UsagePointMicroCheckFactory> microCheckFactories = new CopyOnWriteArrayList<>();
    private List<UsagePointLifeCycleBuilder> builders = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleConfigurationServiceImpl() {
    }

    @Inject // Test
    public UsagePointLifeCycleConfigurationServiceImpl(OrmService ormService,
                                                       NlsService nlsService,
                                                       UpgradeService upgradeService,
                                                       UserService userService,
                                                       FiniteStateMachineService stateMachineService,
                                                       EventService eventService) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setStateMachineService(stateMachineService);
        setEventService(eventService);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(UsagePointLifeCycleConfigurationService.COMPONENT_NAME, "UsagePoint lifecycle configuration");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMicroActionFactory(UsagePointMicroActionFactory microActionFactory) {
        this.microActionFactories.add(microActionFactory);
    }

    @Override
    public void removeMicroActionFactory(UsagePointMicroActionFactory microActionFactory) {
        this.microActionFactories.remove(microActionFactory);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory) {
        this.microCheckFactories.add(microCheckFactory);
    }

    @Override
    public void removeMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory) {
        this.microCheckFactories.remove(microCheckFactory);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder) {
        this.builders.add(builder);
    }

    @Override
    public void removeUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder) {
        this.builders.remove(builder);
    }

    @Override
    public Set<UsagePointStage> getStages() {
        return Stream.of(UsagePointStage.Key.values())
                .map(stage -> new UsagePointStageImpl(stage, this.thesaurus))
                .collect(Collectors.toSet());
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        this.upgradeService.register(InstallIdentifier.identifier("Pulse", UsagePointLifeCycleConfigurationService.COMPONENT_NAME), this.dataModel, Installer.class, Collections.emptyMap());
    }

    public Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UsagePointLifeCycleConfigurationService.class).toInstance(UsagePointLifeCycleConfigurationServiceImpl.this);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(FiniteStateMachineService.class).toInstance(stateMachineService);
                bind(EventService.class).toInstance(eventService);
            }
        };
    }

    @Override
    public String getComponentName() {
        return UsagePointLifeCycleConfigurationService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        keys.addAll(Stream.of(DefaultState.values()).map(DefaultState::getTranslation).collect(Collectors.toList()));
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        keys.addAll(Arrays.asList(Privileges.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Finder<UsagePointLifeCycle> getUsagePointLifeCycles() {
        return DefaultFinder.of(UsagePointLifeCycle.class, where(UsagePointLifeCycleImpl.Fields.OBSOLETE_TIME.fieldName()).isNull(), this.dataModel)
                .defaultSortColumn(UsagePointLifeCycleImpl.Fields.NAME.fieldName());
    }

    @Override
    public Optional<UsagePointLifeCycle> findUsagePointLifeCycle(long id) {
        return this.dataModel.mapper(UsagePointLifeCycle.class).getOptional(id).filter(not(UsagePointLifeCycle::isObsolete));
    }

    @Override
    public Optional<UsagePointLifeCycle> findAndLockUsagePointLifeCycleByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(UsagePointLifeCycle.class).lockObjectIfVersion(version, id).filter(not(UsagePointLifeCycle::isObsolete));
    }

    @Override
    public Optional<UsagePointLifeCycle> findUsagePointLifeCycleByName(String name) {
        Condition condition = where(UsagePointLifeCycleImpl.Fields.NAME.fieldName()).isEqualTo(name)
                .and(where(UsagePointLifeCycleImpl.Fields.OBSOLETE_TIME.fieldName()).isNull());
        return this.dataModel.query(UsagePointLifeCycle.class).select(condition).stream().findFirst();
    }

    @Override
    public UsagePointLifeCycle newUsagePointLifeCycle(String name) {
        UsagePointLifeCycleImpl lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
        FiniteStateMachineBuilder stateMachineBuilder = this.stateMachineService.newFiniteStateMachine(FSM_NAME_PREFIX + name);
        State fsmStateUnderConstruction = stateMachineBuilder.newStandardState(DefaultState.UNDER_CONSTRUCTION.getKey()).complete();
        State fsmStateActive = stateMachineBuilder.newStandardState(DefaultState.ACTIVE.getKey()).complete();
        State fsmStateInactive = stateMachineBuilder.newStandardState(DefaultState.INACTIVE.getKey()).complete();
        State fsmStateDemolished = stateMachineBuilder.newStandardState(DefaultState.DEMOLISHED.getKey()).complete();
        FiniteStateMachine stateMachine = stateMachineBuilder.complete(fsmStateUnderConstruction);

        lifeCycle.setName(name);
        lifeCycle.setStateMachine(stateMachine);
        UsagePointState underConstruction = this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmStateUnderConstruction, UsagePointStage.Key.PRE_OPERATIONAL);
        UsagePointState active = this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmStateActive, UsagePointStage.Key.OPERATIONAL);
        UsagePointState inactive = this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmStateInactive, UsagePointStage.Key.OPERATIONAL);
        UsagePointState demolished = this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmStateDemolished, UsagePointStage.Key.POST_OPERATIONAL);
        lifeCycle.save();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_INSTALL_ACTIVE).format(), underConstruction, active).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_INSTALL_INACTIVE).format(), underConstruction, inactive).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEACTIVATE).format(), active, inactive).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_ACTIVATE).format(), inactive, active).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_ACTIVE).format(), active, demolished).complete();
        lifeCycle.newTransition(this.thesaurus.getFormat(TranslationKeys.TRANSITION_DEMOLISH_FROM_INACTIVE).format(), inactive, demolished).complete();
        this.builders.forEach(builder -> builder.accept(lifeCycle));
        return lifeCycle;
    }

    @Override
    public UsagePointLifeCycle cloneUsagePointLifeCycle(String name, UsagePointLifeCycle source) {
        UsagePointLifeCycleImpl sourceImpl = (UsagePointLifeCycleImpl) source;
        UsagePointLifeCycleImpl lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
        lifeCycle.setName(name);
        FiniteStateMachine stateMachine = this.stateMachineService.cloneFiniteStateMachine(sourceImpl.getStateMachine(), name);
        lifeCycle.setStateMachine(stateMachine);
        lifeCycle.save();
        Map<String, State> targetFsmStates = stateMachine.getStates().stream().collect(Collectors.toMap(State::getName, Function.identity()));
        source.getStates()
                .forEach(usagePointState -> {
                    State state = targetFsmStates.get(((UsagePointStateImpl) usagePointState).getState().getName());
                    if (state == null) {
                        throw new IllegalStateException("Cloned FSM doesn't contain expected state.");
                    }
                    this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, state, usagePointState.getStage().getKey());
                });
        cloneTransitions(sourceImpl, lifeCycle);
        return lifeCycle;
    }

    @Override
    public UsagePointLifeCycle getDefaultLifeCycle() {
        List<UsagePointLifeCycle> defaultLifeCycles = this.dataModel.query(UsagePointLifeCycle.class)
                .select(where(UsagePointLifeCycleImpl.Fields.DEFAULT.fieldName()).isEqualTo(true), Order.NOORDER, true, new String[0], 1, 2);
        if (defaultLifeCycles.size() != 1) {
            throw new IllegalStateException("Default lifecycle is not yet installed");
        }
        return defaultLifeCycles.get(0);
    }

    private void cloneTransitions(UsagePointLifeCycleImpl source, UsagePointLifeCycleImpl target) {
        // clean-up cloned fsm transitions
        FiniteStateMachineUpdater stateMachineUpdater = target.getStateMachine().startUpdate();
        target.getStateMachine().getStates().stream()
                .map(State::getOutgoingStateTransitions)
                .flatMap(Collection::stream)
                .forEach(transition -> stateMachineUpdater.state(transition.getFrom().getId()).prohibit(transition.getEventType()).complete());
        stateMachineUpdater.complete();

        // create new
        Map<String, UsagePointState> statesMap = target.getStates().stream()
                .collect(Collectors.toMap(state -> ((UsagePointStateImpl) state).getState().getName(), Function.identity()));
        source.getTransitions().forEach(sourceTransition -> target.newTransition(sourceTransition.getName(),
                statesMap.get(((UsagePointStateImpl) sourceTransition.getFrom()).getState().getName()),
                statesMap.get(((UsagePointStateImpl) sourceTransition.getTo()).getState().getName()))
                .withLevels(sourceTransition.getLevels())
                .withChecks(sourceTransition.getChecks().stream().map(MicroCheck::getKey).collect(Collectors.toSet()))
                .withActions(sourceTransition.getActions().stream().map(MicroAction::getKey).collect(Collectors.toSet()))
                .triggeredBy(sourceTransition.getTriggeredBy().orElse(null))
                .complete());
    }

    @Override
    public Finder<UsagePointState> getUsagePointStates() {
        return DefaultFinder.of(UsagePointState.class, this.dataModel, State.class);
    }

    @Override
    public Optional<UsagePointState> findUsagePointState(long id) {
        return this.dataModel.mapper(UsagePointState.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointState> findAndLockUsagePointStateByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(UsagePointState.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<UsagePointTransition> findUsagePointTransition(long id) {
        return this.dataModel.mapper(UsagePointTransition.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointTransition> findAndLockUsagePointTransitionByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(UsagePointTransition.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<MicroAction> getMicroActionByKey(String key) {
        return this.microActionFactories.stream()
                .map(factory -> factory.from(key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Set<MicroAction> getMicroActions() {
        return this.microActionFactories.stream()
                .flatMap(factory -> factory.getAllActions().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<MicroCheck> getMicroCheckByKey(String key) {
        return this.microCheckFactories.stream()
                .map(factory -> factory.from(key))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public Set<MicroCheck> getMicroChecks() {
        return this.microCheckFactories.stream()
                .flatMap(factory -> factory.getAllChecks().stream())
                .collect(Collectors.toSet());
    }
}
