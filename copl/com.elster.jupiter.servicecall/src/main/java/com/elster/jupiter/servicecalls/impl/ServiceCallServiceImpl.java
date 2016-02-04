package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.servicecalls.ServiceCallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by bvn on 2/4/16.
 */
@Component(name = "com.elster.jupiter.servicecalls",
        service = {ServiceCallService.class, InstallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + ServiceCallService.COMPONENT_NAME)
public class ServiceCallServiceImpl implements ServiceCallService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider, InstallService {

    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setFiniteStateMachineService(FiniteStateMachineService finiteStateMachineService) {
        this.finiteStateMachineService = finiteStateMachineService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(ServiceCallService.COMPONENT_NAME, "Service calls");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }

    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public String getComponentName() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getModuleName() {
        return ServiceCallService.COMPONENT_NAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        return Collections.emptyList();
    }

    @Override
    public void install() {
        new ServiceCallInstaller().install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, UserService.COMPONENTNAME);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(FiniteStateMachineService.class).toInstance(finiteStateMachineService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

}
