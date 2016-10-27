package com.elster.jupiter.mdm.usagepoint.lifecycle.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineBuilder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.DefaultState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycle;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
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

@Component(name = "com.elster.jupiter.mdm.usagepoint.lifecycle.impl.UsagePointLifeCycleServiceImpl",
        service = {UsagePointLifeCycleService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        immediate = true)
public class UsagePointLifeCycleServiceImpl implements UsagePointLifeCycleService, MessageSeedProvider, TranslationKeyProvider {
    private static final String FSM_NAME_PREFIX = UsagePointLifeCycleService.COMPONENT_NAME + "_";

    private DataModel dataModel;
    private Thesaurus thesaurus;
    private UpgradeService upgradeService;
    private UserService userService;
    private FiniteStateMachineService stateMachineService;
    private EventService eventService;

    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleServiceImpl() {
    }

    @Inject // Test
    public UsagePointLifeCycleServiceImpl(OrmService ormService,
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

    public FiniteStateMachine getDefaultFiniteStateMachine(String name) {
        FiniteStateMachineBuilder stateMachineBuilder = this.stateMachineService.newFiniteStateMachine(FSM_NAME_PREFIX + name);
        stateMachineBuilder.newStandardState(DefaultState.CONNECTED.getKey()).complete();
        stateMachineBuilder.newStandardState(DefaultState.PHYSICALLY_DISCONNECTED.getKey()).complete();
        stateMachineBuilder.newStandardState(DefaultState.DEMOLISHED.getKey()).complete();
        return stateMachineBuilder.complete(stateMachineBuilder.newStandardState(DefaultState.UNDER_CONSTRUCTION.getKey()).complete());
    }

    private UsagePointLifeCycle newUsagePointLifeCycle(String name, FiniteStateMachine stateMachine) {
        UsagePointLifeCycleImpl lifeCycle = this.dataModel.getInstance(UsagePointLifeCycleImpl.class);
        lifeCycle.setName(name);
        lifeCycle.setStateMachine(stateMachine);
        lifeCycle.save();
        return lifeCycle;
    }
    @Override
    public UsagePointLifeCycle newUsagePointLifeCycle(String name) {
        UsagePointLifeCycle lifeCycle = this.newUsagePointLifeCycle(name, getDefaultFiniteStateMachine(name));

        return lifeCycle;
    }

    @Override
    public Optional<UsagePointTransition> finUsagePointTransition(long id) {
        return this.dataModel.mapper(UsagePointTransition.class).getOptional(id);
    }
}
