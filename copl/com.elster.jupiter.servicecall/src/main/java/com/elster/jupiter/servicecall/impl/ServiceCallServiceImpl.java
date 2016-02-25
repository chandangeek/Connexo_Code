package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
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
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.json.JsonService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
public class ServiceCallServiceImpl implements MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider, InstallService, IServiceCallService {

    static final String SERIVCE_CALLS_DESTINATION_NAME = "SerivceCalls";
    static final String SERIVCE_CALLS_SUBSCRIBER_NAME = "SerivceCalls";
    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MessageService messageService;
    private volatile JsonService jsonService;
    private final Map<String, ServiceCallHandler> handlerMap = new ConcurrentHashMap<>();

    // OSGi
    public ServiceCallServiceImpl() {
    }

    @Inject
    public ServiceCallServiceImpl(FiniteStateMachineService finiteStateMachineService, OrmService ormService, NlsService nlsService, CustomPropertySetService customPropertySetService, MessageService messageService, JsonService jsonService) {
        setFiniteStateMachineService(finiteStateMachineService);
        setOrmService(ormService);
        setNlsService(nlsService);
        setMessageService(messageService);
        setJsonService(jsonService);
        setCustomPropertySetService(customPropertySetService);
        activate();
        install();
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
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addServiceCallHandler(ServiceCallHandler serviceCallHandler, Map<String, Object> properties) {
        String name = (String) properties.get("name");
        if (Checks.is(name).emptyOrOnlyWhiteSpace()) {
            throw new MissingHandlerNameException(thesaurus, MessageSeeds.NO_NAME_FOR_HANDLER, serviceCallHandler);
        }
        handlerMap.put(name, serviceCallHandler);
    }

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
        return Collections.emptyList();
    }

    @Override
    public void install() {
        dataModel.getInstance(ServiceCallInstaller.class).install();
    }

    @Override
    public Optional<ServiceCallHandler> findHandler(String handler) {
        return Optional.of(handlerMap.get(handler));
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
                bind(IServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
                bind(JsonService.class).toInstance(jsonService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public Finder<ServiceCallLifeCycle> getServiceCallLifeCycles() {
        return DefaultFinder.of(ServiceCallLifeCycle.class, dataModel)
                .defaultSortColumn(ServiceCallLifeCycleImpl.Fields.name.fieldName());
    }

    @Override
    public Optional<ServiceCallLifeCycle> getServiceCallLifeCycle(String name) {
        return dataModel.mapper(ServiceCallLifeCycle.class)
                .getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), name);
    }

    @Override
    public Optional<ServiceCallLifeCycle> getDefaultServiceCallLifeCycle() {
        return dataModel.mapper(ServiceCallLifeCycle.class)
                .getUnique(ServiceCallLifeCycleImpl.Fields.name.fieldName(), TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME
                        .getKey());
    }

    @Override
    public Finder<ServiceCallType> getServiceCallTypes() {
        return DefaultFinder.of(ServiceCallType.class, dataModel)
                .defaultSortColumn(ServiceCallTypeImpl.Fields.name.fieldName());
    }

    @Override
    public ServiceCallTypeBuilder createServiceCallType(String name, String versionName, ServiceCallLifeCycle serviceCallLifeCycle) {
        return new ServiceCallTypeBuilderImpl(this, name, versionName, (IServiceCallLifeCycle) serviceCallLifeCycle, dataModel);
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

    @Override
    public DestinationSpec getServiceCallQueue() {
        if (!dataModel.isInstalled()) {
            throw new IllegalStateException();
        }
        return messageService.getDestinationSpec(SERIVCE_CALLS_DESTINATION_NAME).get();
    }
}