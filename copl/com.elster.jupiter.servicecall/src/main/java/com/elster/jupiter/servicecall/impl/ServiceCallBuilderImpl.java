package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

class ServiceCallBuilderImpl implements ServiceCallBuilder {
    private final DataModel dataModel;
    private final CustomPropertySetService customPropertySetService;
    private IServiceCallType serviceCallType;
    private ServiceCallImpl parent;
    private ServiceCallImpl instance;
    private final Map<RegisteredCustomPropertySet, PersistentDomainExtension<ServiceCall>> extensions = new HashMap<>();

    @Inject
    ServiceCallBuilderImpl(DataModel dataModel, CustomPropertySetService customPropertySetService) {
        this.dataModel = dataModel;
        this.customPropertySetService = customPropertySetService;
    }

    static ServiceCallBuilderImpl from(DataModel dataModel, IServiceCallType type) {
        return dataModel.getInstance(ServiceCallBuilderImpl.class).init(null, type);
    }

    static ServiceCallBuilderImpl from(DataModel dataModel, ServiceCallImpl parent, IServiceCallType type) {
        return dataModel.getInstance(ServiceCallBuilderImpl.class).init(parent, type);
    }
    private ServiceCallBuilderImpl init(ServiceCallImpl parent, IServiceCallType type) {
        instance = ServiceCallImpl.from(dataModel, parent, type);
        return this;
    }

    ServiceCallBuilderImpl setServiceCallType(IServiceCallType serviceCallType) {
        this.serviceCallType = serviceCallType;
        return this;
    }

    ServiceCallBuilderImpl setParent(ServiceCallImpl parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public ServiceCallBuilder origin(String origin) {
        this.instance.setOrigin(origin);
        return this;
    }

    @Override
    public ServiceCallBuilder externalReference(String externalReference) {
        this.instance.setExternalReference(externalReference);
        return this;
    }

    @Override
    public ServiceCallBuilder targetObject(Object targetObject) {
        this.instance.setTargetObject(targetObject);
        return this;
    }

    @Override
    public ServiceCallBuilder extendedWith(PersistentDomainExtension<ServiceCall> extension) {
        RegisteredCustomPropertySet registeredCustomPropertySet = serviceCallType.getCustomPropertySets()
                .stream()
                .filter(propertySet -> propertySet.getCustomPropertySet()
                        .getPersistenceSupport()
                        .persistenceClass()
                        .isInstance(extension))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
        extensions.put(registeredCustomPropertySet, extension);
        return this;
    }

    @Override
    public ServiceCall create() {
        ServiceCallImpl instance = ServiceCallImpl.from(dataModel, parent, serviceCallType);

        boolean allCustomPropertySetsRegistered = instance.getType().getCustomPropertySets()
                .stream()
                .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().isRequired())
                .allMatch(extensions::containsKey);
        if (!allCustomPropertySetsRegistered) {
            throw new IllegalStateException("Not all custom properties added.");
        }

        instance.save();

        extensions.entrySet()
                .forEach(entry -> {
                    RegisteredCustomPropertySet key = entry.getKey();
                    PersistentDomainExtension<ServiceCall> value = entry.getValue();
                    customPropertySetService.setValuesFor(key.getCustomPropertySet(), instance, value);
                });

        return instance;
    }
}
