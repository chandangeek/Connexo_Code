package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
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
import com.elster.jupiter.servicecall.InvalidPropertySetDomainTypeException;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
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
import java.util.Objects;
import java.util.Optional;

/**
 * Created by bvn on 2/4/16.
 */
@Component(name = "com.elster.jupiter.servicecall",
        service = {ServiceCallService.class, InstallService.class, MessageSeedProvider.class, TranslationKeyProvider.class, PrivilegesProvider.class},
        property = "name=" + ServiceCallService.COMPONENT_NAME,
        immediate = true)
public class ServiceCallServiceImpl implements ServiceCallService, MessageSeedProvider, TranslationKeyProvider, PrivilegesProvider, InstallService {

    private volatile FiniteStateMachineService finiteStateMachineService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;

    // OSGi
    public ServiceCallServiceImpl() {
    }

    @Inject
    public ServiceCallServiceImpl(FiniteStateMachineService finiteStateMachineService, OrmService ormService, NlsService nlsService) {
        this.setFiniteStateMachineService(finiteStateMachineService);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
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
                bind(ServiceCallService.class).toInstance(ServiceCallServiceImpl.this);
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
        return new ServiceCallTypeBuilderImpl(name, versionName, serviceCallLifeCycle);
    }

    @Override
    public Optional<ServiceCallType> findServiceCallType(String name, String versionName) {
        return dataModel.mapper(ServiceCallType.class).getUnique(ServiceCallTypeImpl.Fields.name.fieldName(), name, ServiceCallTypeImpl.Fields.versionName.fieldName(), versionName);
    }


    @Override
    public Optional<ServiceCallType> findAndLockServiceCallType(long id, long version) {
        return dataModel.mapper(ServiceCallType.class).lockObjectIfVersion(version, id);
    }

    @Override
    public ServiceCallLifeCycleBuilder createServiceCallLifeCycle(String name) {
        return dataModel.getInstance(ServiceCallLifeCycleBuilderImpl.class).setName(name);
    }

    class ServiceCallTypeBuilderImpl implements ServiceCallTypeBuilder {
        private final ServiceCallTypeImpl instance;
        private List<RegisteredCustomPropertySet> toBeRegisteredCustomPropertySets = new ArrayList<>();

        public ServiceCallTypeBuilderImpl(String name, String versionName, ServiceCallLifeCycle serviceCallLifeCycle) {
            instance = dataModel.getInstance(ServiceCallTypeImpl.class);
            instance.setName(name);
            instance.setVersionName(versionName);
            instance.setServiceCallLifeCycle(serviceCallLifeCycle);
            instance.setLogLevel(LogLevel.WARNING);
        }

        @Override
        public ServiceCallTypeBuilder logLevel(LogLevel logLevel) {
            Objects.requireNonNull(logLevel, "LogLevel must not be null");
            instance.setLogLevel(logLevel);
            return this;
        }

        @Override
        public ServiceCallTypeBuilder customPropertySet(RegisteredCustomPropertySet customPropertySet) {
            Objects.requireNonNull(customPropertySet);
            if (!customPropertySet.getCustomPropertySet().getDomainClass().isAssignableFrom(ServiceCallType.class)) {
                throw new InvalidPropertySetDomainTypeException(thesaurus, MessageSeeds.INVALID_CPS_TYPE, customPropertySet);
            }
            this.toBeRegisteredCustomPropertySets.add(customPropertySet);
            return this;
        }

        @Override
        public ServiceCallType add() {
            instance.save();
            for (RegisteredCustomPropertySet customPropertySet : toBeRegisteredCustomPropertySets) {
                instance.addCustomPropertySet(customPropertySet);
            }

            return instance;
        }
    }
}
