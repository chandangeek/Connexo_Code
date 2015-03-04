package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.FinateStateMachine;
import com.elster.jupiter.fsm.FinateStateMachineBuilder;
import com.elster.jupiter.fsm.FinateStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
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
import java.util.Optional;

/**
 * Provides an implementation for the {@link FinateStateMachineService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (16:50)
 */
@Component(name = "com.elster.jupiter.fsm", service = {FinateStateMachineService.class, ServerFinateStateMachineService.class, InstallService.class, TranslationKeyProvider.class}, property = "name=" + FinateStateMachineService.COMPONENT_NAME)
@SuppressWarnings("unused")
public class FinateStateMachineServiceImpl implements ServerFinateStateMachineService, InstallService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile NlsService nlsService;
    private volatile UserService userService;
    private volatile EventService eventService;
    private Thesaurus thesaurus;

    // For OSGi purposes
    public FinateStateMachineServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public FinateStateMachineServiceImpl(OrmService ormService, NlsService nlsService, UserService userService, EventService eventService) {
        this();
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.activate();
        this.install();
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.userService, eventService).install(true);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return FinateStateMachineService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR");
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
                bind(FinateStateMachineService.class).toInstance(FinateStateMachineServiceImpl.this);
                bind(ServerFinateStateMachineService.class).toInstance(FinateStateMachineServiceImpl.this);
                bind(UserService.class).toInstance(userService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(FinateStateMachineService.COMPONENT_NAME, "Finate State Machine");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(FinateStateMachineService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public StateTransitionEventType newStateTransitionEventType(String symbol) {
        return this.dataModel.getInstance(StateTransitionEventTypeImpl.class).initialize(symbol);
    }

    @Override
    public Optional<StateTransitionEventType> findStateTransitionEventTypeBySymbol(String symbol) {
        return this.dataModel
                .mapper(StateTransitionEventType.class)
                .getUnique(StateTransitionEventTypeImpl.Fields.SYMBOL.fieldName(), symbol);
    }

    @Override
    public FinateStateMachineBuilder newFinateStateMachine(String name, String topic) {
        FinateStateMachineImpl stateMachine = this.dataModel.getInstance(FinateStateMachineImpl.class).initialize(name, topic);
        return new FinateStateMachineBuilderImpl(dataModel, stateMachine);
    }

    @Override
    public Optional<FinateStateMachine> findFinateStateMachineByName(String name) {
        return this.dataModel
                .mapper(FinateStateMachine.class)
                .getUnique(FinateStateMachineImpl.Fields.NAME.fieldName(), name);
    }

}