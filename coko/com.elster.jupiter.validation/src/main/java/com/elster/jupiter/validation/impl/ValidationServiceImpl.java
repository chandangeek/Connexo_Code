package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + Bus.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements ValidationService, InstallService, ServiceLocator{

    private static final Upcast<IValidationRuleSet,ValidationRuleSet> UPCAST = new Upcast<>();
    private static final Upcast<IValidationRule,ValidationRule> RULE_UPCAST = new Upcast<>();
    private volatile OrmClient ormClient;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private volatile List<ValidatorFactory> validatorFactories = new ArrayList<>();
    private volatile DataModel dataModel;

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(Clock clock, EventService eventService, MeteringService meteringService, OrmService ormService) {
        this.clock = clock;
        this.eventService = eventService;
        this.meteringService = meteringService;
        setOrmService(ormService);
        activate();
        install();
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(EventService.class).toInstance(eventService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataModel.class).toInstance(dataModel);
            }
        });
    }

    @Deactivate
    public void deactivate() {
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
    public MeteringService getMeteringService() {
        return meteringService;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        this.ormClient = new OrmClientImpl(dataModel);
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
    public ValidationRuleSet createValidationRuleSet(String name, String description) {
        return new ValidationRuleSetImpl(name, description);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        return getOrmClient().getValidationRuleSetFactory().getOptional(id).transform(UPCAST);
    }

    @Override
    public Optional<ValidationRule> getValidationRule(long id) {
        return getOrmClient().getValidationRuleFactory().getOptional(id).transform(RULE_UPCAST);
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return new ArrayList<ValidationRuleSet>(getOrmClient().getValidationRuleSetFactory().find());
    }

    @Override
    public void validate(MeterActivation meterActivation, Interval interval) {
        Optional<MeterActivationValidation> found = getOrmClient().getMeterActivationValidationFactory().getOptional(meterActivation.getId());
        if (found.isPresent()) {
            found.get().validate(interval);
        }

    }

    @Override
     public List<String> getAvailableValidatorNames() {
        List<String> result = new ArrayList<String>();
        for (ValidatorFactory factory : validatorFactories) {
            for (String implementation : factory.available()) {
                result.add(implementation);
            }
        }
        return result;
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
        Optional<MeterActivationValidation> found = Bus.getOrmClient().getMeterActivationValidationFactory().getOptional(meterActivation.getId());
        MeterActivationValidation meterActivationValidation = found.or(new MeterActivationValidationImpl(meterActivation));
        meterActivationValidation.setRuleSet(ruleSet);

        for (Channel channel : meterActivation.getChannels()) {
            ChannelValidation channelValidation = meterActivationValidation.addChannelValidation(channel);
        }

        meterActivationValidation.save();
    }


}
