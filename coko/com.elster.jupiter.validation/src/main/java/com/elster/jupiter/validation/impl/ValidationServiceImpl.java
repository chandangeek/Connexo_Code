package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
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

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public class ValidationServiceImpl implements ValidationService, InstallService {

    private static final Upcast<IValidationRuleSet,ValidationRuleSet> UPCAST = new Upcast<>();
    private static final Upcast<IValidationRule,ValidationRule> RULE_UPCAST = new Upcast<>();
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private volatile List<ValidatorFactory> validatorFactories = new ArrayList<>();
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;

    public ValidationServiceImpl() {
    }

    @Inject
    ValidationServiceImpl(Clock clock, EventService eventService, MeteringService meteringService, OrmService ormService, QueryService queryService, NlsService nlsService) {
        this.clock = clock;
        this.eventService = eventService;
        this.meteringService = meteringService;
        setQueryService(queryService);
        setOrmService(ormService);
        setNlsService(nlsService);
        activate();
        install();
    }

    @Activate
    public void activate() {
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Clock.class).toInstance(clock);
                bind(EventService.class).toInstance(eventService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataModel.class).toInstance(dataModel);
                bind(ValidationService.class).toInstance(ValidationServiceImpl.this);
                bind(ValidatorCreator.class).toInstance(new DefaultValidatorCreator());
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel, eventService).install(true, true);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(ValidationService.COMPONENTNAME, "Validation");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
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

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ValidationService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name) {
        return ValidationRuleSetImpl.from(dataModel, name);
    }

    @Override
    public ValidationRuleSet createValidationRuleSet(String name, String description) {
        ValidationRuleSet set = ValidationRuleSetImpl.from(dataModel, name, description);
        set.save();
        return set;
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        return dataModel.mapper(IValidationRuleSet.class).getOptional(id).transform(UPCAST);
    }

    @Override
    public Optional<ValidationRule> getValidationRule(long id) {
        return dataModel.mapper(ValidationRule.class).getOptional(id);
    }

    @Override
    public Query<ValidationRuleSet> getRuleSetQuery() {
        return queryService.wrap(
                dataModel.query(
                        ValidationRuleSet.class
                )
        );
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return new ArrayList<>(dataModel.mapper(ValidationRuleSet.class).find());
    }

    @Override
    public void validate(MeterActivation meterActivation, Interval interval) {
        Optional<MeterActivationValidation> found = dataModel.mapper(MeterActivationValidation.class).getOptional(meterActivation.getId());
        if (found.isPresent()) {
            found.get().validate(interval);
        }

    }

    @Override
     public List<Validator> getAvailableValidators() {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        List<Validator> result = new ArrayList<Validator>();
        for (ValidatorFactory factory : validatorFactories) {
            for (String implementation : factory.available()) {
                Validator validator = validatorCreator.getTemplateValidator(implementation);
                result.add(validator);
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

    class DefaultValidatorCreator implements ValidatorCreator {

        @Override
        public Validator getValidator(String implementation, Map<String, Quantity> props) {
            for (ValidatorFactory factory : validatorFactories) {
                if (factory.available().contains(implementation)) {
                    return factory.create(implementation, props);
                }
            }
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }

        public Validator getTemplateValidator(String implementation) {
            for (ValidatorFactory factory : validatorFactories) {
                if (factory.available().contains(implementation)) {
                    return factory.createTemplate(implementation);
                }
            }
            throw new ValidatorNotFoundException(thesaurus, implementation);
        }
    }

    @Override
    public void applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation) {
        Optional<MeterActivationValidation> found = dataModel.mapper(MeterActivationValidation.class).getOptional(meterActivation.getId());
        MeterActivationValidation meterActivationValidation = found.or(MeterActivationValidationImpl.from(dataModel, meterActivation));
        meterActivationValidation.setRuleSet(ruleSet);

        for (Channel channel : meterActivation.getChannels()) {
            ChannelValidation channelValidation = meterActivationValidation.addChannelValidation(channel);
        }

        meterActivationValidation.save();
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
