package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroActionFactory;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointMicroCheckFactory;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions.MicroActionTranslationKeys;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks.MicroCheckTranslationKeys;
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
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "UsagePointLifeCycleServiceImpl",
        service = {UsagePointLifeCycleService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true)
public class UsagePointLifeCycleServiceImpl implements UsagePointLifeCycleService, MessageSeedProvider, TranslationKeyProvider {
    private DataModel dataModel;
    private Thesaurus thesaurus;
    private UpgradeService upgradeService;
    private UserService userService;
    private FiniteStateMachineService stateMachineService;
    private EventService eventService;
    private UsagePointMicroActionFactory microActionFactory;
    private UsagePointMicroCheckFactory microCheckFactory;

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleServiceImpl() {
    }

    @Inject // Test
    public UsagePointLifeCycleServiceImpl(OrmService ormService,
                                          NlsService nlsService,
                                          UpgradeService upgradeService,
                                          UserService userService,
                                          FiniteStateMachineService stateMachineService,
                                          EventService eventService,
                                          UsagePointMicroActionFactory microActionFactory,
                                          UsagePointMicroCheckFactory microCheckFactory) {
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setStateMachineService(stateMachineService);
        setEventService(eventService);
        setMicroActionFactory(microActionFactory);
        setMicroCheckFactory(microCheckFactory);
        activate();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(UsagePointLifeCycleService.COMPONENT_NAME, "UsagePoint lifecycle");
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
        this.thesaurus = nlsService.getThesaurus(UsagePointLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
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

    @Reference
    public void setMicroActionFactory(UsagePointMicroActionFactory microActionFactory) {
        this.microActionFactory = microActionFactory;
    }

    @Reference
    public void setMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory) {
        this.microCheckFactory = microCheckFactory;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        this.upgradeService.register(InstallIdentifier.identifier("Insight", UsagePointLifeCycleService.COMPONENT_NAME), this.dataModel, Installer.class, Collections.emptyMap());

    }

    public Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UsagePointLifeCycleService.class).toInstance(UsagePointLifeCycleServiceImpl.this);
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
        return UsagePointLifeCycleService.COMPONENT_NAME;
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
        keys.addAll(Arrays.asList(MicroCategoryTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroActionTranslationKeys.values()));
        keys.addAll(Arrays.asList(MicroCheckTranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Optional<UsagePointLifeCycle> findUsagePointLifeCycle(long id) {
        return this.dataModel.mapper(UsagePointLifeCycle.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointLifeCycle> findAndLockUsagePointLifeCycleByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(UsagePointLifeCycle.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<UsagePointLifeCycle> findUsagePointLifeCycleByName(String name) {
        Condition condition = where(UsagePointLifeCycleImpl.Fields.NAME.fieldName()).isEqualTo(name)
                .and(where(UsagePointLifeCycleImpl.Fields.OBSOLETE_TIME.fieldName()).isNull());
        return this.dataModel.query(UsagePointLifeCycle.class).select(condition).stream().findFirst();
    }

    @Override
    public UsagePointLifeCycle newUsagePointLifeCycle(String name) {
        return this.dataModel.getInstance(UsagePointLifeCycleBuilderImpl.class).getDefaultLifeCycleWithName(name);
    }

    @Override
    public UsagePointLifeCycle cloneUsagePointLifeCycle(String name, UsagePointLifeCycle source) {
        return this.dataModel.getInstance(UsagePointLifeCycleBuilderImpl.class).cloneUsagePointLifeCycle(name, source);
    }

    @Override
    public Optional<UsagePointState> findUsagePointState(long id) {
        Optional<State> fsmState = this.stateMachineService.findFiniteStateById(id);
        if (fsmState.isPresent()) {
            FiniteStateMachine stateMachine = fsmState.get().getFiniteStateMachine();
            return this.dataModel.query(UsagePointLifeCycle.class).select(where(UsagePointLifeCycleImpl.Fields.STATE_MACHINE.fieldName()).isEqualTo(stateMachine))
                    .stream()
                    .map(lifeCycle -> this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmState.get()))
                    .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public Optional<UsagePointState> findAndLockUsagePointStateByIdAndVersion(long id, long version) {
        Optional<State> fsmState = this.stateMachineService.findAndLockStateByIdAndVersion(id, version);
        if (fsmState.isPresent()) {
            FiniteStateMachine stateMachine = fsmState.get().getFiniteStateMachine();
            return this.dataModel.query(UsagePointLifeCycle.class).select(where(UsagePointLifeCycleImpl.Fields.STATE_MACHINE.fieldName()).isEqualTo(stateMachine))
                    .stream()
                    .map(lifeCycle -> this.dataModel.getInstance(UsagePointStateImpl.class).init(lifeCycle, fsmState.get()))
                    .findFirst();
        }
        return Optional.empty();
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
    public MicroAction getMicroActionByKey(MicroAction.Key key) {
        return this.microActionFactory.from(key);
    }

    @Override
    public MicroCheck getMicroCheckByKey(MicroCheck.Key key) {
        return this.microCheckFactory.from(key);
    }
}
