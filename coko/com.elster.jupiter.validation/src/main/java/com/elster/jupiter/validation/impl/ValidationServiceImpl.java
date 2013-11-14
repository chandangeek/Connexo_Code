package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.validation.*;
import com.google.common.base.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements ValidationService, InstallService, ServiceLocator{

    private volatile OrmClient ormClient;
    private volatile ComponentCache componentCache;
    private volatile EventService eventService;
    private volatile List<ValidatorFactory> validatorFactories = new ArrayList<ValidatorFactory>();

    @Activate
    public void activate(BundleContext context) {
        Bus.setServiceLocator(this);
    }

    @Deactivate
    public void deactivate(BundleContext context) {
        Bus.clearServiceLocator(this);
    }

    @Override
    public void install() {
        new InstallerImpl().install(true, true);
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public ComponentCache getComponentCache() {
        return componentCache;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
        this.componentCache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name) {
        return new ValidationRuleSetImpl(name);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        return getOrmClient().getValidationRuleSetFactory().get(id);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
    public void addResource(ValidatorFactory validatorfactory) {
        validatorFactories.add(validatorfactory);
    }

    public void removeResource(ValidatorFactory validatorfactory) {
        validatorFactories.remove(validatorfactory);
    }

    @Override
    public Validator getValidator(String implementation) {
        for (ValidatorFactory factory : validatorFactories) {
            if (factory.available().get().contains(implementation)) {
                return factory.create(implementation);
            }
        }
        throw new ValidatorNotFoundException(implementation);
    }

    @Override
    public ValidationService getValidationService() {
        return this;
    }
}
