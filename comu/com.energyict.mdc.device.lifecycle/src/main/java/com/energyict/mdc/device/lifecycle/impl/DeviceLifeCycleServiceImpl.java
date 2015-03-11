package com.energyict.mdc.device.lifecycle.impl;

import com.energyict.mdc.device.lifecycle.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;

import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineService;
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
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceLifeCycleService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (10:44)
 */
@Component(name = "com.energyict.device.lifecycle", service = {DeviceLifeCycleService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + DeviceLifeCycleService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class DeviceLifeCycleServiceImpl implements DeviceLifeCycleService, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile FinateStateMachineService stateMachineService;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public DeviceLifeCycleServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DeviceLifeCycleServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, TransactionService transactionService, FinateStateMachineService stateMachineService) {
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
        new Installer(this.dataModel, this.userService, this.transactionService, this.stateMachineService).install(true, true);
    }

    private void install(boolean transactional) {
        new Installer(this.dataModel, this.userService, this.transactionService, this.stateMachineService).install(transactional, true);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return DeviceLifeCycleService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM");
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

                bind(DeviceLifeCycleService.class).toInstance(DeviceLifeCycleServiceImpl.this);
            }
        };
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(DeviceLifeCycleService.COMPONENT_NAME, "Device Life Cycle");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
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
    public void setStateMachineService(FinateStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @Override
    public DeviceLifeCycle newDeviceLifeCycleUsing(FinateStateMachine finateStateMachine) {
        return this.dataModel.getInstance(DeviceLifeCycleImpl.class).initialize(finateStateMachine);
    }

}