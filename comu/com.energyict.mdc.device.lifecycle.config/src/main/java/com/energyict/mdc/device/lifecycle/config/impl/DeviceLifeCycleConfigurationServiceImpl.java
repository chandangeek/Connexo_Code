package com.energyict.mdc.device.lifecycle.config.impl;

import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedBusinessProcessAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleBuilder;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

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
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    private volatile TransactionService transactionService;
    private volatile FiniteStateMachineService stateMachineService;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public DeviceLifeCycleConfigurationServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleConfigurationServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, TransactionService transactionService, FiniteStateMachineService stateMachineService) {
        this();
        this.setOrmService(ormService);
        this.setUserService(userService);
        this.setNlsService(nlsService);
        this.setTransactionService(transactionService);
        this.setStateMachineService(stateMachineService);
        this.activate();
        this.install(false);    // Requires all test classes to run in a transactional environment
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.userService, this.transactionService, this.stateMachineService, this).install(true, true);
    }

    private void install(boolean transactional) {
        new Installer(this.dataModel, this.userService, this.transactionService, this.stateMachineService, this).install(transactional, true);
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        Stream.of(MessageSeeds.values()).forEach(keys::add);
        Stream.of(DefaultState.values()).forEach(keys::add);
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
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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

}