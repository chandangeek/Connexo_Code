package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.Upcast;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.*;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.validation", service = {InstallService.class, ValidationService.class}, property = "name=" + ValidationService.COMPONENTNAME, immediate = true)
public final class ValidationServiceImpl implements ValidationService, InstallService {

    private static final Upcast<IValidationRuleSet, ValidationRuleSet> UPCAST = new Upcast<>();
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Clock clock;
    private volatile List<ValidatorFactory> validatorFactories = new ArrayList<>();
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile List<ValidationRuleSetResolver> ruleSetResolvers = new ArrayList<>();

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
    public final void activate() {
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
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        });
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public void install() {
        new InstallerImpl(dataModel, eventService, thesaurus).install(true, true);
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
    public MeterValidation createMeterValidation(MeterActivation activation) {
        MeterValidation meterValidation = MeterValidationImpl.from(dataModel, activation);
        meterValidation.save();
        return meterValidation;
    }

    @Override
    public Optional<MeterValidation> getMeterValidation(MeterActivation meterActivation) {
        return dataModel.mapper(MeterValidation.class).getOptional(meterActivation.getId());
    }

    @Override
    public void disableMeterValidation(MeterActivation meterActivation) {
        Optional<MeterValidation> meterValidationRef = getMeterValidation(meterActivation);
        if(!meterValidationRef.isPresent()) {
            meterValidationRef = Optional.of(createMeterValidation(meterActivation));
        }
        MeterValidation meterValidation = meterValidationRef.get();
        meterValidation.setActivationStatus(false);
        meterValidation.save();
    }

    @Override
    public void enableMeterValidation(MeterActivation meterActivation, Optional<Date> dateRef) {
        Optional<MeterValidation> meterValidationRef = getMeterValidation(meterActivation);
        if(!meterValidationRef.isPresent()) {
            meterValidationRef = Optional.of(createMeterValidation(meterActivation));
        }
        MeterValidation meterValidation = meterValidationRef.get();
        meterValidation.setActivationStatus(true);
        meterValidation.save();

        if (dateRef.isPresent()) {
            Date date = dateRef.get();
            List<MeterActivationValidation> validations = getMeterActivationValidationsForMeterActivation(meterActivation);
            for(MeterActivationValidation validation : validations) {
                for(ChannelValidation channelValidation : validation.getChannelValidations()) {
                    channelValidation.setLastChecked(date);
                }
                validation.save();
            }
            validate(meterActivation, Interval.startAt(date));
        }
    }

    @Override
    public Date getLastChecked(MeterActivation meterActivation) {
        List<MeterActivationValidation> validations = getMeterActivationValidationsForMeterActivation(meterActivation);
        Set<ChannelValidation> chennelValidations = new LinkedHashSet<>();
        for(MeterActivationValidation validation : validations) {
            chennelValidations.addAll(validation.getChannelValidations());
        }
        List<Date> lastCheckedDates = new ArrayList();

        for(ChannelValidation channelValidation : chennelValidations) {
            lastCheckedDates.add(channelValidation.getLastChecked());
        }

        return lastCheckedDates.isEmpty() ? new Date() : Collections.min(lastCheckedDates);
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(long id) {
        Optional<ValidationRuleSet> ruleSetRef = dataModel.mapper(IValidationRuleSet.class).getOptional(id).transform(UPCAST);
        if (ruleSetRef.isPresent() && ruleSetRef.get().getObsoleteDate() != null) {
            ruleSetRef = Optional.absent();
        }
        return ruleSetRef;
    }

    @Override
    public Optional<ValidationRuleSet> getValidationRuleSet(String name) {
        Condition condition = Operator.EQUAL.compare("name", name).and(Operator.ISNULL.compare("obsoleteTime"));
        List<ValidationRuleSet> ruleSets = getRuleSetQuery().select(condition);
        return ruleSets.isEmpty() ? Optional.<ValidationRuleSet>absent() : Optional.of(ruleSets.get(0));
    }

    @Override
    public Optional<ValidationRule> getValidationRule(long id) {
        Optional<ValidationRule> ruleRef = dataModel.mapper(ValidationRule.class).getOptional(id);
        if(ruleRef.isPresent() && ruleRef.get().getObsoleteDate() != null) {
            ruleRef = Optional.absent();
        }
        return ruleRef;
    }

    @Override
    public Query<ValidationRuleSet> getRuleSetQuery() {
        Query<ValidationRuleSet> ruleSetQuery = queryService.wrap(dataModel.query(ValidationRuleSet.class));
        ruleSetQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleSetQuery;
    }

    @Override
    public List<ValidationRuleSet> getValidationRuleSets() {
        return getRuleSetQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }

    @Override
    public void validate(MeterActivation meterActivation, Interval interval) {
        List<IMeterActivationValidation> meterActivationValidations = getMeterActivationValidations(meterActivation, interval);
        for (IMeterActivationValidation meterActivationValidation : meterActivationValidations) {
            meterActivationValidation.validate(interval);
        }
    }

    public List<IMeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation, Interval interval) {
        return manageMeterActivationValidations(meterActivation, interval);
    }

    private List<IMeterActivationValidation> manageMeterActivationValidations(MeterActivation meterActivation, Interval interval) {
        List<ValidationRuleSet> ruleSets = new ArrayList<>();
        for (ValidationRuleSetResolver ruleSetResolver : ruleSetResolvers) {
            ruleSets.addAll(ruleSetResolver.resolve(meterActivation, interval));
        }
        List<IMeterActivationValidation> existingMeterActivationValidations = getMeterActivationValidations(meterActivation);
        List<IMeterActivationValidation> returnList = new ArrayList<>();
        for (ValidationRuleSet ruleSet : ruleSets) {
            Optional<IMeterActivationValidation> meterActivationValidation = getForRuleSet(existingMeterActivationValidations, ruleSet);
            if (!meterActivationValidation.isPresent()) {
                returnList.add(applyRuleSet(ruleSet, meterActivation));
            } else {
                returnList.add(meterActivationValidation.get());
            }
        }
        for (IMeterActivationValidation existingMeterActivationValidation : existingMeterActivationValidations) {
            if (!ruleSets.contains(existingMeterActivationValidation.getRuleSet())) {
                existingMeterActivationValidation.makeObsolete();
            }
        }
        return returnList;
    }

    private Optional<IMeterActivationValidation> getForRuleSet(List<IMeterActivationValidation> meterActivations, ValidationRuleSet ruleSet) {
        for (IMeterActivationValidation meterActivation : meterActivations) {
            if (ruleSet.equals(meterActivation.getRuleSet())) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.absent();
    }

    private List<IMeterActivationValidation> getMeterActivationValidations(MeterActivation meterActivation) {
        Condition condition = where("meterActivation").isEqualTo(meterActivation).and(where("obsoleteTime").isNull());
        return dataModel.query(IMeterActivationValidation.class).select(condition);
    }

    private IMeterActivationValidation applyRuleSet(ValidationRuleSet ruleSet, MeterActivation meterActivation) {
        IMeterActivationValidation meterActivationValidation = MeterActivationValidationImpl.from(dataModel, meterActivation);
        meterActivationValidation.setRuleSet(ruleSet);

        for (Channel channel : meterActivation.getChannels()) {
            meterActivationValidation.addChannelValidation(channel);
        }

        meterActivationValidation.save();
        return meterActivationValidation;
    }

    @Override
    public List<MeterActivationValidation> getMeterActivationValidationsForMeterActivation(MeterActivation meterActivation) {
        List<IMeterActivationValidation> meterActivationValidationsList = getMeterActivationValidations(meterActivation);
        List<MeterActivationValidation> result = new ArrayList<>();
        for(IMeterActivationValidation inner : meterActivationValidationsList) {
            result.add(inner);
        }
        return result;
    }

    @Override
    public Validator getValidator(String implementation) {
        ValidatorCreator validatorCreator = new DefaultValidatorCreator();
        return validatorCreator.getTemplateValidator(implementation);
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.add(resolver);
    }

    public void removeValidationRuleSetResolver(ValidationRuleSetResolver resolver) {
        ruleSetResolvers.remove(resolver);
    }

    DataModel getDataModel() {
        return dataModel;
    }
}
