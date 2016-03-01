package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
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
import com.elster.jupiter.servicecall.MissingHandlerNameException;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
import com.elster.jupiter.servicecall.security.Privileges;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bvn on 2/4/16.
 */
@Component(name = "com.elster.jupiter.servicecall",
        service = {ServiceCallService.class, InstallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + ServiceCallService.COMPONENT_NAME,
        immediate = true)
public class ServiceCallServiceImpl implements IServiceCallService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider, InstallService {

    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private final Map<String, ServiceCallHandler> handlerMap = new ConcurrentHashMap<>();
    private volatile CustomPropertySetService customPropertySetService;
    private volatile UserService userService;

    // OSGi
    public ServiceCallServiceImpl() {
    }

    @Inject
    public ServiceCallServiceImpl(FiniteStateMachineService finiteStateMachineService, OrmService ormService, NlsService nlsService, UserService userService, CustomPropertySetService customPropertySetService) {
        this.setFiniteStateMachineService(finiteStateMachineService);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setUserService(userService);
        setCustomPropertySetService(customPropertySetService);
        activate();
        this.install();
    }

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
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ServiceCallService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    @Override
    public void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw new MissingHandlerNameException(thesaurus, MessageSeeds.NO_NAME_FOR_HANDLER, serviceCallHandler);
        }
        handlerMap.put(name, serviceCallHandler);
    }

    @Override
    public void removeServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        handlerMap.remove(name);
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
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_SERVICE_CALL_TYPES.getKey(), Privileges.RESOURCE_SERVICE_CALL_TYPES_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES, Privileges.Constants.VIEW_SERVICE_CALL_TYPES)));
        return resources;
    }

    @Override
    public void install() {
        dataModel.getInstance(ServiceCallInstaller.class).install();
    }

    @Override
    public Optional<ServiceCallHandler> findHandler(String handler) {
        if (Checks.is(handler).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlerMap.get(handler));
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME,
                UserService.COMPONENTNAME,
                FiniteStateMachineService.COMPONENT_NAME,
                CustomPropertySetService.COMPONENT_NAME);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(FiniteStateMachineService.class).toInstance(finiteStateMachineService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(ServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
                bind(IServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public Finder<ServiceCallLifeCycle> getServiceCallLifeCycles() {
        return DefaultFinder.of(ServiceCallLifeCycle.class, dataModel).defaultSortColumn(ServiceCallLifeCycleImpl.Fields.name.fieldName());
    }

    @Override
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name) {
        return dataModel.mapper(ServiceCallLifeCycle.class).getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), name);
    }

    @Override
    public Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle() {
        return dataModel.mapper(ServiceCallLifeCycle.class).getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey());
    }

    @Override
    public Finder<ServiceCallType> getServiceCallTypes() {
        return DefaultFinder.of(ServiceCallType.class, dataModel).defaultSortColumn(ServiceCallTypeImpl.Fields.name.fieldName());
    }

    @Override
    public ServiceCallTypeBuilder createServiceCallType(String name, String versionName, ServiceCallLifeCycle serviceCallLifeCycle) {
        return new ServiceCallTypeBuilderImpl(this, name, versionName, (IServiceCallLifeCycle) serviceCallLifeCycle, dataModel, thesaurus);
    }

    @Override
    public Optional<ServiceCallType> findServiceCallType(String name, String versionName) {
        return dataModel.mapper(IServiceCallType.class)
                .getUnique(ServiceCallTypeImpl.Fields.name.fieldName(), name, ServiceCallTypeImpl.Fields.versionName.fieldName(), versionName)
                .map(ServiceCallType.class::cast);
    }


    @Override
    public Optional<ServiceCallType> findAndLockServiceCallType(long id, long version) {
        return dataModel.mapper(IServiceCallType.class)
                .lockObjectIfVersion(version, id)
                .map(ServiceCallType.class::cast);
    }

    @Override
    public ServiceCallLifeCycleBuilder createServiceCallLifeCycle(String name) {
        return dataModel.getInstance(ServiceCallLifeCycleBuilderImpl.class).setName(name);
    }

    @Override
    public Collection<String> findAllHandlers() {
        return handlerMap.keySet();
    }

    @Override
    public Optional<ServiceCall> getServiceCall(long id) {
        return dataModel.mapper(ServiceCall.class).getOptional(id);
    }
}
