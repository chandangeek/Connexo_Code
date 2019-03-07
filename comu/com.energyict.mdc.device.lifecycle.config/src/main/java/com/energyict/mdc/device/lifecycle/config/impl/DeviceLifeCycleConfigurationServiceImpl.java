/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.issue.share.IssueCreationValidator;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_6SimpleUpgrader;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceMicroCheckFactory;
import com.energyict.mdc.device.lifecycle.config.MicroCheckNew;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcess;
import com.energyict.mdc.device.lifecycle.config.TransitionBusinessProcessInUseException;
import com.energyict.mdc.device.lifecycle.config.UnknownTransitionBusinessProcessException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceLifeCycleConfigurationService} interface.
 */
@Component(name = "com.energyict.device.lifecycle.config", service = {DeviceLifeCycleConfigurationService.class,
        TranslationKeyProvider.class, MessageSeedProvider.class, IssueCreationValidator.class},
        property = "name=" + DeviceLifeCycleConfigurationService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleConfigurationServiceImpl implements DeviceLifeCycleConfigurationService,
        TranslationKeyProvider, MessageSeedProvider, IssueCreationValidator {

    private static final Logger LOGGER = Logger.getLogger(DeviceLifeCycleConfigurationServiceImpl.class.getName());

    private final Set<Privilege> privileges = new HashSet<>();

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile FiniteStateMachineService stateMachineService;
    private volatile EventService eventService;
    private volatile UpgradeService upgradeService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    private List<DeviceMicroCheckFactory> microCheckFactories = new CopyOnWriteArrayList<>();

    // For OSGi purposes
    public DeviceLifeCycleConfigurationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleConfigurationServiceImpl(OrmService ormService, NlsService nlsService, UserService userService,
                                                   FiniteStateMachineService stateMachineService, EventService eventService,
                                                   UpgradeService upgradeService, MeteringService meteringService) {
        this();
        setOrmService(ormService);
        setUserService(userService);
        setNlsService(nlsService);
        setStateMachineService(stateMachineService);
        setEventService(eventService);
        setUpgradeService(upgradeService);
        setMeteringService(meteringService);
        activate();
        initializeTestPrivileges();
    }

    private void initializeTestPrivileges() {
        this.getModuleResources().stream()
                .filter(Objects::nonNull)
                .forEach(resource -> this.userService.saveResourceWithPrivileges(resource.getComponentName(), resource.getName(), resource.getDescription(), resource.getPrivilegeNames()
                        .toArray(new String[resource.getPrivilegeNames().size()])));
        this.initializePrivileges();
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(DefaultLifeCycleTranslationKey.values()).forEach(keys::add);
        Stream.of(DefaultState.values()).forEach(keys::add);
        Stream.of(EndDeviceStageTranslationKey.values()).forEach(keys::add);
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return DeviceLifeCycleConfigurationService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Activate
    public void activate() {
        dataModel.register(this.getModule());
        upgradeService.register(InstallIdentifier.identifier("MultiSense", DeviceLifeCycleConfigurationService.COMPONENT_NAME),
                dataModel, Installer.class,
                ImmutableMap.of(
                        Version.version(10, 2), UpgraderV10_2.class,
                        Version.version(10, 3), UpgraderV10_3.class,
                        Version.version(10, 4), UpgraderV10_4.class,
                        Version.version(10, 6), V10_6SimpleUpgrader.class));
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
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(EventService.class).toInstance(eventService);
                bind(FiniteStateMachineService.class).toInstance(stateMachineService);
                bind(UserService.class).toInstance(userService);

                bind(DeviceLifeCycleConfigurationService.class).toInstance(DeviceLifeCycleConfigurationServiceImpl.this);
            }
        };
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceLifeCycleConfigurationService.COMPONENT_NAME, "Device Life Cycle");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleConfigurationService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    private void initializePrivileges() {
        this.privileges.clear();
        this.userService
                .getPrivileges()
                .forEach(this::addPrivilegeIfFound);
    }

    private void addPrivilegeIfFound(Privilege privilege) {
        AuthorizedAction.Level.forPrivilege(privilege.getName()).ifPresent(level -> this.privileges.add(privilege));
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
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public DeviceLifeCycleBuilderImpl newDeviceLifeCycleUsing(String name, FiniteStateMachine finiteStateMachine) {
        return new DeviceLifeCycleBuilderImpl(this.dataModel, this.dataModel.getInstance(DeviceLifeCycleImpl.class).initialize(name, finiteStateMachine));
    }

    @Override
    public DeviceLifeCycle newDefaultDeviceLifeCycle(String name) {
        return dataModel.getInstance(Installer.class).createDefaultLifeCycle(name, LOGGER);
    }

    @Override
    public DeviceLifeCycle cloneDeviceLifeCycle(DeviceLifeCycle source, String name) {
        FiniteStateMachine clonedStateMachine = this.stateMachineService.cloneFiniteStateMachine(source.getFiniteStateMachine(), name);
        DeviceLifeCycleBuilderImpl builder = this.newDeviceLifeCycleUsing(name, clonedStateMachine);
        source.getAuthorizedActions().forEach(a -> this.cloneAction(a, builder, clonedStateMachine));
        DeviceLifeCycle cloned = builder.complete();
        cloned.save();
        return cloned;
    }

    private void cloneAction(AuthorizedAction sourceAction, DeviceLifeCycleBuilder builder, FiniteStateMachine clonedFiniteStateMachine) {
        if (sourceAction instanceof AuthorizedBusinessProcessAction) {
            AuthorizedBusinessProcessAction sourceBusinessProcessAction = (AuthorizedBusinessProcessAction) sourceAction;
            builder
                    .newCustomAction(
                            clonedFiniteStateMachine.getState(sourceBusinessProcessAction.getState().getName()).get(),
                            sourceBusinessProcessAction.getName(),
                            sourceBusinessProcessAction.getTransitionBusinessProcess())
                    .addAllLevels(sourceBusinessProcessAction.getLevels())
                    .complete();
        } else {
            AuthorizedTransitionAction sourceAuthorizedTransitionAction = (AuthorizedTransitionAction) sourceAction;
            builder
                    .newTransitionAction(this.findClonedTransition(sourceAuthorizedTransitionAction.getStateTransition(), clonedFiniteStateMachine))
                    .setChecks(sourceAuthorizedTransitionAction.getChecks().stream().map(MicroCheckNew::getKey).collect(Collectors.toSet()))
                    .addActions(sourceAuthorizedTransitionAction.getActions())
                    .addAllLevels(sourceAuthorizedTransitionAction.getLevels())
                    .complete();
        }
    }

    private StateTransition findClonedTransition(StateTransition sourceTransition, FiniteStateMachine clonedFiniteStateMachine) {
        return clonedFiniteStateMachine
                .getTransitions()
                .stream()
                .filter(t -> t.getFrom().getName().equals(sourceTransition.getFrom().getName()))
                .filter(t -> t.getTo().getName().equals(sourceTransition.getTo().getName()))
                .findFirst()
                .get(); // Cloning of the FiniteStateMachine was done milliseconds ago so both states should have been cloned as well as the transition
    }

    @Override
    public Optional<DeviceLifeCycle> findDeviceLifeCycle(long id) {
        return this.dataModel.mapper(DeviceLifeCycle.class).getOptional(id);
    }

    @Override
    public Optional<DeviceLifeCycle> findAndLockDeviceLifeCycleByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(DeviceLifeCycle.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<DeviceLifeCycle> findDeviceLifeCycleByName(String name) {
        Condition condition = where(DeviceLifeCycleImpl.Fields.NAME.fieldName()).isEqualTo(name)
                .and(where(DeviceLifeCycleImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).isNull());
        List<DeviceLifeCycle> deviceLifeCycles = this.dataModel.query(DeviceLifeCycle.class).select(condition);
        // Expecting at most one
        if (deviceLifeCycles.isEmpty()) {
            return Optional.empty();
        } else if (deviceLifeCycles.size() > 1) {
            throw new NotUniqueException(name);
        } else {
            return Optional.of(deviceLifeCycles.get(0));
        }
    }

    @Override
    public Optional<DeviceLifeCycle> findDefaultDeviceLifeCycle() {
        return this.findDeviceLifeCycleByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());
    }

    @Override
    public Finder<DeviceLifeCycle> findAllDeviceLifeCycles() {
        return DefaultFinder.of(
                DeviceLifeCycle.class,
                where(DeviceLifeCycleImpl.Fields.OBSOLETE_TIMESTAMP.fieldName()).isNull(),
                this.dataModel,
                AuthorizedAction.class, // join actions and finite state machine details
                FiniteStateMachine.class, State.class, StateTransition.class,
                StateTransitionEventType.class, ProcessReference.class);
    }

    @Override
    public Optional<Privilege> findInitiateActionPrivilege(String privilegeName) {
        if (this.privileges.isEmpty()) {
            this.initializePrivileges();
        }
        return this.privileges
                .stream()
                .filter(p -> p.getName().equals(privilegeName))
                .findAny();
    }

    public Map<Locale, String> getAllTranslationsForKey(String translationKey) {
        return userService.getUserPreferencesService().getSupportedLocales()
                .stream()
                .collect(Collectors.toMap(Function.identity(), locale -> thesaurus.getString(locale, translationKey, translationKey)));
    }

    @Override
    public TimeDuration getMaximumFutureEffectiveTimeShift() {
        return EffectiveTimeShift.FUTURE.maximumValue();
    }

    @Override
    public TimeDuration getDefaultFutureEffectiveTimeShift() {
        return EffectiveTimeShift.FUTURE.defaultValue();
    }

    @Override
    public TimeDuration getMaximumPastEffectiveTimeShift() {
        return EffectiveTimeShift.PAST.maximumValue();
    }

    @Override
    public TimeDuration getDefaultPastEffectiveTimeShift() {
        return EffectiveTimeShift.PAST.defaultValue();
    }

    @Override
    public List<TransitionBusinessProcess> findTransitionBusinessProcesses() {
        return this.dataModel.mapper(TransitionBusinessProcess.class).find();
    }

    @Override
    public TransitionBusinessProcess enableAsTransitionBusinessProcess(String name, String deploymentId, String processId) {
        TransitionBusinessProcessImpl businessProcess = this.dataModel.getInstance(TransitionBusinessProcessImpl.class).initialize(name, deploymentId, processId);
        Save.CREATE.validate(this.dataModel, businessProcess);
        this.dataModel.persist(businessProcess);
        return businessProcess;
    }

    @Override
    public void disableAsTransitionBusinessProcess(String deploymentId, String processId) {
        List<TransitionBusinessProcess> businessProcesses = this.dataModel
                .mapper(TransitionBusinessProcess.class)
                .find(
                        TransitionBusinessProcessImpl.Fields.DEPLOYMENT_ID.fieldName(), deploymentId,
                        TransitionBusinessProcessImpl.Fields.PROCESS_ID.fieldName(), processId);
        if (businessProcesses.isEmpty()) {
            throw new UnknownTransitionBusinessProcessException(this.thesaurus, MessageSeeds.NO_SUCH_PROCESS, deploymentId, processId);
        } else {
            Condition condition = Where.where(AuthorizedActionImpl.Fields.PROCESS.fieldName()).in(businessProcesses);
            List<AuthorizedBusinessProcessAction> actions = this.dataModel.mapper(AuthorizedBusinessProcessAction.class).select(condition);
            if (actions.isEmpty()) {
                businessProcesses.forEach(this.dataModel::remove);
            } else {
                throw new TransitionBusinessProcessInUseException(this.thesaurus, MessageSeeds.TRANSITION_PROCESS_IN_USE, businessProcesses.get(0));
            }
        }
    }

    @Override
    public Optional<AuthorizedAction> findAuthorizedActionById(long id) {
        return this.dataModel.mapper(AuthorizedAction.class).getOptional(id);
    }

    @Override
    public Optional<AuthorizedAction> findAndLockAuthorizedActionByIdAndVersion(long id, long version) {
        return this.dataModel.mapper(AuthorizedAction.class).lockObjectIfVersion(version, id);
    }

    List<ResourceDefinition> getModuleResources() {
        return Arrays.asList(
                this.userService.createModuleResourceWithPrivileges(DeviceLifeCycleConfigurationService.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_LIFECYCLE.getKey(), Privileges.RESOURCE_DEVICE_LIFECYCLE_DESCRIPTION.getKey(),
                        Arrays.asList(Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE, Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE)),
                this.userService.createModuleResourceWithPrivileges(DeviceLifeCycleConfigurationService.COMPONENT_NAME, Privileges.RESOURCE_DEVICE_LIFECYCLE_LEVELS.getKey(), Privileges.RESOURCE_DEVICE_LIFECYCLE_LEVELS_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.INITIATE_ACTION_1,
                                Privileges.Constants.INITIATE_ACTION_2,
                                Privileges.Constants.INITIATE_ACTION_3,
                                Privileges.Constants.INITIATE_ACTION_4))
        );
    }

    public boolean isValidCreationEvent(IssueEvent issueEvent) {
        EnumSet<EndDeviceStage> restrictedStages = EnumSet.of(EndDeviceStage.PRE_OPERATIONAL, EndDeviceStage.POST_OPERATIONAL);
        Optional<EndDevice> endDevice = issueEvent.getEndDevice();
        if (endDevice.isPresent()) {
            String stateName = endDevice.get().getState().orElseThrow(() -> new IllegalStateException("Device does not have a state"))
                    .getStage().orElseThrow(() -> new IllegalStateException("Device does not have a stage"))
                    .getName();
            return !restrictedStages.contains(EndDeviceStage.fromKey(stateName));
        }
        return true;
    }

    @Override
    public String getDisplayName(DefaultState state) {
        return this.thesaurus.getFormat(state).format();
    }

    @Override
    public String getStageDisplayName(EndDeviceStage stage) {
        return this.thesaurus.getString(EndDeviceStageTranslationKey.prefix + stage.getKey(), stage.getKey());
    }

    @Override
    public Set<MicroCheckNew> getMicroChecks() {
        return this.microCheckFactories.stream()
                .flatMap(factory -> factory.getAllChecks().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<MicroCheckNew> getMicroCheckByKey(String microCheckKey) {
        return this.microCheckFactories
                .stream()
                .map(factory -> factory.from(microCheckKey))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .map(MicroCheckNew.class::cast);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC)
    public void addMicroCheckFactory(DeviceMicroCheckFactory microCheckFactory) {
        this.microCheckFactories.add(microCheckFactory);
    }

    @Override
    public void removeMicroCheckFactory(DeviceMicroCheckFactory microCheckFactory) {
        this.microCheckFactories.remove(microCheckFactory);
    }
}
