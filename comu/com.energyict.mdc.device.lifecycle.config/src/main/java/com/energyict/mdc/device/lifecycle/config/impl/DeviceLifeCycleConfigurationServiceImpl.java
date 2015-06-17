package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.ProcessReference;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link DeviceLifeCycleConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:44)
 */
@Component(name = "com.energyict.device.lifecycle.config", service = {DeviceLifeCycleConfigurationService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + DeviceLifeCycleConfigurationService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleConfigurationServiceImpl implements DeviceLifeCycleConfigurationService, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile FiniteStateMachineService stateMachineService;
    private Thesaurus thesaurus;
    private final Set<Privilege> privileges = new HashSet<>();

    // For OSGi purposes
    public DeviceLifeCycleConfigurationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleConfigurationServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, FiniteStateMachineService stateMachineService) {
        this();
        this.setOrmService(ormService);
        this.setUserService(userService);
        this.setNlsService(nlsService);
        this.setStateMachineService(stateMachineService);
        this.activate();
        this.install();
        this.initializePrivileges();
    }

    @Override
    public void install() {
        this.getInstaller().install(true);
    }

    private Installer getInstaller() {
        return new Installer(this.dataModel, this.userService, this.stateMachineService, this);
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(MessageSeeds.values()).forEach(keys::add);
        Stream.of(DefaultLifeCycleTranslationKey.values()).forEach(keys::add);
        return keys;
    }

    @Override
    public String getComponentName() {
        return DeviceLifeCycleConfigurationService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", FiniteStateMachineService.COMPONENT_NAME);
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
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);

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
        this.initializePrivileges();
    }

    private void initializePrivileges() {
        this.privileges.clear();
        this.userService
            .getResources(Installer.PRIVILEGES_COMPONENT)
            .stream()
            .flatMap(r -> r.getPrivileges().stream())
            .forEach(this::addPrivilegeIfFound);
    }

    private void addPrivilegeIfFound(Privilege privilege) {
        AuthorizedAction.Level.forPrivilege(privilege.getName()).ifPresent(level -> this.privileges.add(privilege));
    }

    @Reference
    public void setStateMachineService(FiniteStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Override
    public DeviceLifeCycleBuilderImpl newDeviceLifeCycleUsing(String name, FiniteStateMachine finiteStateMachine) {
        return new DeviceLifeCycleBuilderImpl(this.dataModel, this.dataModel.getInstance(DeviceLifeCycleImpl.class).initialize(name, finiteStateMachine));
    }

    @Override
    public DeviceLifeCycle newDefaultDeviceLifeCycle(String name) {
        return this.getInstaller().createDefaultLifeCycle(name);
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
                    sourceBusinessProcessAction.getDeploymentId(),
                    sourceBusinessProcessAction.getProcessId())
                .addAllLevels(sourceBusinessProcessAction.getLevels())
                .complete();
        }
        else {
            AuthorizedTransitionAction sourceAuthorizedTransitionAction = (AuthorizedTransitionAction) sourceAction;
            builder
                .newTransitionAction(this.findClonedTransition(sourceAuthorizedTransitionAction.getStateTransition(), clonedFiniteStateMachine))
                .addAllChecks(sourceAuthorizedTransitionAction.getChecks())
                .addAllActions(sourceAuthorizedTransitionAction.getActions())
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
    public Optional<DeviceLifeCycle> findDeviceLifeCycleByName(String name) {
        return this.dataModel.mapper(DeviceLifeCycle.class).getUnique(DeviceLifeCycleImpl.Fields.NAME.fieldName(), name);
    }

    @Override
    public Optional<DeviceLifeCycle> findDefaultDeviceLifeCycle() {
        return this.findDeviceLifeCycleByName(DefaultLifeCycleTranslationKey.DEFAULT_DEVICE_LIFE_CYCLE_NAME.getKey());
    }

    @Override
    public Finder<DeviceLifeCycle> findAllDeviceLifeCycles() {
        return DefaultFinder.of(
                DeviceLifeCycle.class,
                this.dataModel,
                AuthorizedAction.class, // join actions and finite state machine details
                FiniteStateMachine.class, State.class, StateTransition.class,
                StateTransitionEventType.class, ProcessReference.class);
    }

    @Override
    public Optional<Privilege> findInitiateActionPrivilege(String privilegeName) {
        return this.privileges
                .stream()
                .filter(p -> p.getName().equals(privilegeName))
                .findAny();
    }

}