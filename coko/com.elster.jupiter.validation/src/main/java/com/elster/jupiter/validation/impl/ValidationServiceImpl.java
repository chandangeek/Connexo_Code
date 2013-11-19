package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.*;
import com.google.common.base.Optional;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements ValidationService, InstallService, ServiceLocator{

    private volatile OrmClient ormClient;
    private volatile ComponentCache componentCache;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
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
    public MeteringService getMeteringService() {
        return meteringService;
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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Clock getClock() {
        return clock;
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name) {
        return new ValidationRuleSetImpl(name);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        return getOrmClient().getValidationRuleSetFactory().get(id);
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return getOrmClient().getValidationRuleSetFactory().find();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
    public void addResource(ValidatorFactory validatorfactory) {
        validatorFactories.add(validatorfactory);
    }

    public void removeResource(ValidatorFactory validatorfactory) {
        validatorFactories.remove(validatorfactory);
    }

    @Override
    public Validator getValidator(String implementation, Map<String, Quantity> props) {
        for (ValidatorFactory factory : validatorFactories) {
            if (factory.available().contains(implementation)) {
                return factory.create(implementation, props);
            }
        }
        throw new ValidatorNotFoundException(implementation);
    }

    @Override
    public ValidationService getValidationService() {
        return this;
    }

    @Override
    public void applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation) {
        Optional<MeterActivationValidation> found = Bus.getOrmClient().getMeterActivationValidationFactory().get(meterActivation.getId());
        MeterActivationValidation meterActivationValidation = found.or(new MeterActivationValidationImpl(meterActivation));
        meterActivationValidation.setRuleSet(ruleSet);

        for (Channel channel : meterActivation.getChannels()) {
            ChannelValidation channelValidation = meterActivationValidation.addChannelValidation(channel);
        }

        meterActivationValidation.save();
    }


}
